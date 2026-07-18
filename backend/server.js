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

    try {
        if (provider === 'PHONE') {
            if (twilioClient) {
                await twilioClient.messages.create({
                    body: `Your chatBoat verification code is: ${code}`,
                    from: process.env.TWILIO_PHONE_NUMBER,
                    to: identifier
                });
                console.log(`REAL SMS sent to ${identifier}`);
            } else {
                console.log(`\n*****************************************`);
                console.log(`[MOCK SMS] FOR: ${identifier}`);
                console.log(`[MOCK SMS] CODE: ${code}`);
                console.log(`*****************************************\n`);
            }
        } else {
            if (transporter) {
                await transporter.sendMail({
                    from: `"chatBoat" <${process.env.EMAIL_USER}>`,
                    to: identifier,
                    subject: "Verification Code for chatBoat",
                    html: `
                        <div style="font-family: sans-serif; max-width: 600px; margin: auto; padding: 40px; border: 1px solid #f0f0f0; border-radius: 12px;">
                            <h1 style="color: #0B57D0; text-align: center;">chatBoat</h1>
                            <div style="background-color: #f8f9fa; padding: 30px; border-radius: 12px; text-align: center;">
                                <p style="font-size: 16px;">Your verification code is:</p>
                                <h2 style="font-size: 42px; letter-spacing: 10px; color: #0B57D0;">${code}</h2>
                                <p style="font-size: 13px; color: #70757a;">Valid for 10 minutes.</p>
                            </div>
                            <p style="text-align: center; color: #9aa0a6; font-size: 12px; margin-top: 30px;">chatBoat AI Technologies Inc.</p>
                        </div>
                    `
                });
                console.log(`REAL Email sent to ${identifier}`);
            } else {
                console.log(`\n*****************************************`);
                console.log(`[MOCK EMAIL] FOR: ${identifier}`);
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
