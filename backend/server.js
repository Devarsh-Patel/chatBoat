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
                    from: `"chatBoat Auth" <${process.env.EMAIL_USER}>`,
                    to: identifier,
                    subject: "Your Verification Code",
                    text: `Your chatBoat verification code is: ${code}. It will expire in 10 minutes.`
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
