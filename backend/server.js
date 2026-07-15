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
                        <div style="font-family: sans-serif; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #eee; border-radius: 10px;">
                            <div style="text-align: center; margin-bottom: 20px;">
                                <h1 style="color: #0B57D0; margin: 0;">chatBoat</h1>
                                <p style="color: #666; font-size: 14px;">Your Intelligent AI Search Companion</p>
                            </div>
                            <div style="background-color: #f9f9f9; padding: 20px; border-radius: 8px; text-align: center;">
                                <p style="font-size: 16px; color: #333;">Hello,</p>
                                <p style="font-size: 16px; color: #333;">Use the following verification code to access your chatBoat account:</p>
                                <h2 style="font-size: 32px; letter-spacing: 5px; color: #0B57D0; margin: 20px 0;">${code}</h2>
                                <p style="font-size: 14px; color: #888;">This code will expire in 10 minutes. If you didn't request this, please ignore this email.</p>
                            </div>
                            <hr style="border: 0; border-top: 1px solid #eee; margin: 30px 0;">
                            <div style="font-size: 12px; color: #999; text-align: center;">
                                <p><strong>chatBoat AI Technologies Inc.</strong></p>
                                <p>123 Innovation Drive, Silicon Valley, CA</p>
                                <p>This is an automated message, please do not reply.</p>
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
