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
const PORT = process.env.PORT || 8080;
const GEMINI_API_KEY = process.env.GEMINI_API_KEY;

// Middleware for logging all requests
app.use((req, res, next) => {
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
    if (Object.keys(req.body).length > 0) {
        console.log('Body:', JSON.stringify(req.body, null, 2));
    }
    next();
});

// Email Config
let transporter = null;
if (process.env.EMAIL_USER && process.env.EMAIL_PASS) {
    transporter = nodemailer.createTransport({
        service: 'gmail',
        auth: {
            user: process.env.EMAIL_USER,
            pass: process.env.EMAIL_PASS
        }
    });
    console.log("Email service initialized.");
} else {
    console.log("WARNING: Email credentials missing. Email will be MOCKED in logs.");
}

// SMS Config (Twilio)
let twilioClient = null;
if (process.env.TWILIO_SID && process.env.TWILIO_SID.startsWith('AC')) {
    twilioClient = twilio(process.env.TWILIO_SID, process.env.TWILIO_AUTH_TOKEN);
    console.log("Twilio service initialized.");
} else {
    console.log("WARNING: Twilio credentials missing. SMS will be MOCKED in logs.");
}

// Temporary store
const verificationCodes = new Map();

app.get('/health', (req, res) => {
    res.json({ status: 'UP', timestamp: new Date() });
});

app.post('/api/auth/send-code', async (req, res) => {
    const { provider, identifier, code } = req.body;

    if (!identifier || !code) {
        return res.status(400).json({ success: false, message: "Missing identifier or code" });
    }

    try {
        if (provider === 'PHONE') {
            // Enhanced Phone Verification: Normalizing phone number (should start with +)
            const phone = identifier.startsWith('+') ? identifier : `+${identifier}`;

            if (twilioClient) {
                await twilioClient.messages.create({
                    body: `Your chatBoat verification code is: ${code}`,
                    from: process.env.TWILIO_PHONE_NUMBER,
                    to: phone
                });
                console.log(`REAL SMS sent to ${phone}`);
            } else {
                console.log(`\n*****************************************`);
                console.log(`[MOCK SMS] TO: ${phone}`);
                console.log(`[MOCK SMS] CODE: ${code}`);
                console.log(`*****************************************\n`);
            }
        } else {
            // Email Verification (Google / Apple)
            if (transporter) {
                await transporter.sendMail({
                    from: `"chatBoat" <${process.env.EMAIL_USER}>`,
                    to: identifier,
                    subject: "Verification Code for chatBoat",
                    html: `
                        <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: auto; padding: 40px; border: 1px solid #e0e0e0; border-radius: 16px; background-color: white;">
                            <div style="text-align: center; margin-bottom: 32px;">
                                <div style="display: inline-block; width: 64px; height: 64px; background-color: #0B57D0; border-radius: 16px; color: white; font-size: 32px; font-weight: bold; line-height: 64px; margin-bottom: 12px;">cB</div>
                                <h1 style="color: #1a1a1a; margin: 0; font-size: 24px;">Verify your identity</h1>
                                <p style="color: #666; font-size: 14px; margin-top: 4px;">Security code for your chatBoat account</p>
                            </div>

                            <div style="background-color: #f8f9fa; padding: 32px; border-radius: 12px; text-align: center; border: 1px solid #f1f3f4;">
                                <p style="font-size: 16px; color: #3c4043; margin-bottom: 24px;">Enter this code in the app to continue:</p>
                                <div style="font-size: 48px; font-weight: 700; letter-spacing: 12px; color: #0B57D0; font-family: monospace;">
                                    ${code}
                                </div>
                                <p style="font-size: 12px; color: #5f6368; margin-top: 32px;">This code will expire in 10 minutes for your security.</p>
                            </div>

                            <div style="margin-top: 40px; text-align: center; border-top: 1px solid #eeeeee; padding-top: 24px;">
                                <p style="font-size: 12px; color: #999; margin: 0;"><strong>chatBoat AI Technologies</strong></p>
                                <p style="font-size: 11px; color: #999; margin: 4px 0;">If you did not request this code, you can safely ignore this email.</p>
                            </div>
                        </div>
                    `
                });
                console.log(`REAL Email sent to ${identifier}`);
            } else {
                console.log(`\n*****************************************`);
                console.log(`[MOCK EMAIL] TO: ${identifier}`);
                console.log(`[MOCK EMAIL] CODE: ${code}`);
                console.log(`*****************************************\n`);
            }
        }

        verificationCodes.set(identifier, { code, expires: Date.now() + 600000 });
        res.json({ success: true, message: "Code sent!" });
    } catch (error) {
        console.error("Auth Error:", error.message);
        res.status(500).json({ success: false, message: error.message });
    }
});

app.post('/api/auth/verify-code', (req, res) => {
    const { identifier, code } = req.body;
    const stored = verificationCodes.get(identifier);

    if (stored && stored.code === code && stored.expires > Date.now()) {
        verificationCodes.delete(identifier);
        res.json({ success: true });
    } else {
        res.status(400).json({ success: false, message: "Invalid or expired code." });
    }
});

app.post('/api/ai/chat', async (req, res) => {
    try {
        const response = await axios.post(
            `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${GEMINI_API_KEY}`,
            req.body
        );
        res.json(response.data);
    } catch (error) {
        console.error("AI Error:", error.response?.data || error.message);
        res.status(500).json({ success: false });
    }
});

app.listen(PORT, '0.0.0.0', () => {
    console.log(`\n>>> chatBoat server is LIVE on port ${PORT}`);
    console.log(`>>> Listening on http://localhost:${PORT}`);
    console.log(`>>> Access from Emulator at http://10.0.2.2:${PORT}\n`);
});
