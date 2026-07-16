require('dotenv').config();
const express = require('express');
const nodemailer = require('nodemailer');
const twilio = require('twilio');
const axios = require('axios');
const cors = require('cors');

const app = express();
app.use(express.json());
app.use(cors());

// --- Configuration ---
const PORT = process.env.PORT || 3000;
const GEMINI_API_KEY = process.env.GEMINI_API_KEY;

// Email Config (Example using Gmail - requires App Password)
let transporter = null;
if (process.env.EMAIL_USER && process.env.EMAIL_PASS) {
    transporter = nodemailer.createTransport({
        service: 'gmail',
        auth: {
            user: process.env.EMAIL_USER,
            pass: process.env.EMAIL_PASS
        }
    });
} else {
    console.log("WARNING: Email credentials missing in .env. Email will be mocked.");
}

// SMS Config (Twilio)
let twilioClient = null;
if (process.env.TWILIO_SID && process.env.TWILIO_SID.startsWith('AC')) {
    twilioClient = twilio(process.env.TWILIO_SID, process.env.TWILIO_AUTH_TOKEN);
} else {
    console.log("WARNING: Twilio SID is missing or invalid. SMS will be mocked.");
}

// Temporary in-memory store for verification codes (Use Redis for production)
const verificationCodes = new Map();

// --- Auth Endpoints ---

app.post('/api/auth/send-code', async (req, res) => {
    console.log("--- Received Send Code Request ---");
    console.log(req.body);
    const { provider, identifier, code } = req.body;

    try {
        if (provider === 'PHONE') {
            if (twilioClient) {
                await twilioClient.messages.create({
                    body: `Your chatBoat verification code is: ${code}`,
                    from: process.env.TWILIO_PHONE_NUMBER,
                    to: identifier
                });
            } else {
                console.log(`[MOCK SMS] Code ${code} sent to ${identifier}`);
            }
        } else {
            // Only try to send email if user has configured it
            if (transporter) {
                await transporter.sendMail({
                    from: `"chatBoat" <${process.env.EMAIL_USER}>`,
                    to: identifier,
                    subject: "Verification Code for chatBoat",
                    html: `
                        <div style="font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; max-width: 600px; margin: auto; padding: 40px; border: 1px solid #f0f0f0; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05);">
                            <div style="text-align: center; margin-bottom: 30px;">
                                <div style="display: inline-block; width: 80px; height: 80px; background-color: #0B57D0; border-radius: 20px; line-height: 80px; margin-bottom: 15px; color: white; font-size: 40px; font-weight: bold;">cB</div>
                                <h1 style="color: #0B57D0; margin: 0; font-size: 28px; letter-spacing: -0.5px;">chatBoat</h1>
                                <p style="color: #5f6368; font-size: 14px; margin-top: 5px;">Intelligent AI Search Companion</p>
                            </div>

                            <div style="background-color: #f8f9fa; padding: 30px; border-radius: 12px; text-align: center; border: 1px solid #e8eaed;">
                                <p style="font-size: 16px; color: #3c4043; margin-bottom: 20px;">Use the following verification code to access your account:</p>
                                <div style="font-size: 42px; font-weight: bold; letter-spacing: 8px; color: #0B57D0; background: white; padding: 20px; border-radius: 8px; display: inline-block; border: 1px solid #dadce0;">
                                    ${code}
                                </div>
                                <p style="font-size: 13px; color: #70757a; margin-top: 25px;">This code expires in 10 minutes. <br/>If you didn't request this, please ignore this email.</p>
                            </div>

                            <div style="margin-top: 40px; padding-top: 30px; border-top: 1px solid #f0f0f0; color: #9aa0a6; font-size: 12px; text-align: center; line-height: 1.6;">
                                <p style="margin: 0; font-weight: bold; color: #5f6368;">chatBoat AI Technologies Inc.</p>
                                <p style="margin: 2px 0;">123 Innovation Drive, Silicon Valley, CA 94025</p>
                                <p style="margin: 10px 0;">&copy; 2025 chatBoat. All rights reserved.</p>
                                <p style="margin-top: 15px;">This is an automated security message. Please do not reply.</p>
                            </div>
                        </div>
                    `
                });
            } else {
                console.log(`[MOCK EMAIL] Code ${code} sent to ${identifier}`);
            }
        }

        // Store code for verification (hashed in real app)
        verificationCodes.set(identifier, { code, expires: Date.now() + 600000 });

        res.json({ success: true, message: "Code sent successfully!" });
    } catch (error) {
        console.error("Auth Error:", error);
        res.status(500).json({ success: false, message: "Failed to send code." });
    }
});

app.post('/api/auth/verify-code', (req, res) => {
    const { identifier, code } = req.body;
    const stored = verificationCodes.get(identifier);

    if (stored && stored.code === code && stored.expires > Date.now()) {
        verificationCodes.delete(identifier);
        res.json({ success: true, message: "Verified!" });
    } else {
        res.status(400).json({ success: false, message: "Invalid or expired code." });
    }
});

// --- AI Endpoint (Gemini Proxy) ---

app.post('/api/ai/chat', async (req, res) => {
    const { contents } = req.body;

    try {
        const response = await axios.post(
            `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${GEMINI_API_KEY}`,
            { contents }
        );
        res.json(response.data);
    } catch (error) {
        console.error("AI Error:", error.response?.data || error.message);
        res.status(500).json({ success: false, message: "AI failed to respond." });
    }
});

app.listen(PORT, '0.0.0.0', () => {
    console.log(`chatBoat Backend running on http://0.0.0.0:${PORT}`);
});
