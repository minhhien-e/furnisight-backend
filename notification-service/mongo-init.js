db = db.getSiblingDB("notification_db");

db.notification_templates.insertOne({
  _id: UUID("22222222-2222-2222-2222-222222222222"),
  code: "media-uploaded",
  name: "Media Uploaded Notification",
  variables: ["fileName", "uploaderName"],
  titleTemplate: "New media uploaded",
  bodyTemplate: "Your file {{fileName}} was uploaded successfully by {{uploaderName}}.",
  type: "MEDIA",
  channel: "IN_APP",
  defaultImage: "",
  defaultActionUrl: "",
  createdAt: new Date(),
  updatedAt: new Date(),
  version: NumberLong(0)
});

db.notification_templates.insertOne({
  _id: UUID("7d9e4a3e-1f5b-48af-91cd-b2354cf1af91"),
  code: "account-verify-code",
  name: "Account Verification Email Template",
  variables: ["verifyUrl"],
  titleTemplate: "Verify Your Account",
  bodyTemplate: `<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style>
    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
    .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
    .header { text-align: center; border-bottom: 2px solid #ee4d2d; padding-bottom: 10px; margin-bottom: 20px; }
    .header h2 { color: #ee4d2d; margin: 0; text-transform: uppercase; font-size: 24px; }
    .content { font-size: 16px; color: #333333; line-height: 1.5; text-align: center; }
    .button {
        display: inline-block;
        margin: 30px 0;
        padding: 12px 24px;
        font-size: 16px;
        color: #ffffff !important;
        background-color: #ee4d2d;
        text-decoration: none;
        border-radius: 5px;
        font-weight: bold;
    }
    .button:hover {
        background-color: #d8431f;
    }
    .footer { font-size: 14px; color: #777777; text-align: center; border-top: 1px solid #eaeaea; padding-top: 10px; margin-top: 20px; }
</style>
</head>
<body>
<div class="container">
    <div class="header">
        <h2>Account Service</h2>
    </div>
    <div class="content">
        <p>Hello,</p>
        <p>Please click the button below to verify your account:</p>

        <a href="{{verifyUrl}}" class="button">Verify Account</a>

        <p>If the button doesn't work, copy and paste this link into your browser:</p>
        <p><a href="{{verifyUrl}}">{{verifyUrl}}</a></p>

        <p>If you did not request this, please ignore this email.</p>
    </div>
    <div class="footer">
        <p>&copy; Account Service. All rights reserved.</p>
    </div>
</div>
</body>
</html>`,
  type: "SYSTEM",
  channel: "EMAIL",
  defaultImage: "",
  defaultActionUrl: "",
  createdAt: new Date(),
  updatedAt: new Date(),
  version: NumberLong(0)
});

db.notification_templates.insertOne({
  _id: UUID("8a9c3d4f-5e6b-7c8d-9e0f-1a2b3c4d5e6f"),
  code: "account-reset-password",
  name: "Account Reset Password Email Template",
  variables: ["token"],
  titleTemplate: "[Account Service] Your Password Reset Code",
  bodyTemplate: `<!DOCTYPE html>
<html>
<head>
<style>
    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
    .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
    .header { text-align: center; border-bottom: 2px solid #ee4d2d; padding-bottom: 10px; margin-bottom: 20px; }
    .header h2 { color: #ee4d2d; margin: 0; text-transform: uppercase; font-size: 24px; }
    .content { font-size: 16px; color: #333333; line-height: 1.5; }
    .verification-code { display: block; margin: 30px 0; font-size: 32px; font-weight: bold; color: #ee4d2d; text-align: center; letter-spacing: 5px; background: #fff5f5; padding: 15px; border-radius: 4px; border: 1px dashed #ee4d2d; }
    .footer { font-size: 14px; color: #777777; text-align: center; border-top: 1px solid #eaeaea; padding-top: 10px; margin-top: 20px; }
</style>
</head>
<body>
<div class="container">
    <div class="header">
        <h2>FurniSight</h2>
    </div>
    <div class="content">
        <p>Hello,</p>
        <p>We received a request to reset your password. Please use the following code to proceed:</p>
        <span class="verification-code">{{token}}</span>
        <p>If you did not request this code, please safely ignore this email.</p>
    </div>
    <div class="footer">
        <p>&copy; Account Service. All rights reserved.</p>
    </div>
</div>
</body>
</html>`,
  type: "SYSTEM",
  channel: "EMAIL",
  defaultImage: "",
  defaultActionUrl: "",
  createdAt: new Date(),
  updatedAt: new Date(),
  version: NumberLong(0)
});

db.notification_templates.insertOne({
  _id: UUID("1b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6e"),
  code: "account-email-change",
  name: "Account Email Change OTP Template",
  variables: ["otpCode"],
  titleTemplate: "[FurniSight] Your Email Change OTP",
  bodyTemplate: `<!DOCTYPE html>
<html>
<head>
<style>
    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
    .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
    .header { text-align: center; border-bottom: 2px solid #ee4d2d; padding-bottom: 10px; margin-bottom: 20px; }
    .header h2 { color: #ee4d2d; margin: 0; text-transform: uppercase; font-size: 24px; }
    .content { font-size: 16px; color: #333333; line-height: 1.5; }
    .verification-code { display: block; margin: 30px 0; font-size: 32px; font-weight: bold; color: #ee4d2d; text-align: center; letter-spacing: 5px; background: #fff5f5; padding: 15px; border-radius: 4px; border: 1px dashed #ee4d2d; }
    .footer { font-size: 14px; color: #777777; text-align: center; border-top: 1px solid #eaeaea; padding-top: 10px; margin-top: 20px; }
</style>
</head>
<body>
<div class="container">
    <div class="header">
        <h2>FurniSight</h2>
    </div>
    <div class="content">
        <p>Hello,</p>
        <p>We received a request to change your email address. Please use the following OTP to proceed:</p>
        <span class="verification-code">{{otpCode}}</span>
        <p>If you did not request this code, please safely ignore this email.</p>
    </div>
    <div class="footer">
        <p>&copy; FurniSight. All rights reserved.</p>
    </div>
</div>
</body>
</html>`,
  type: "SYSTEM",
  channel: "EMAIL",
  defaultImage: "",
  defaultActionUrl: "",
  createdAt: new Date(),
  updatedAt: new Date(),
  version: NumberLong(0)
});

// ─── VERIFY CURRENT EMAIL (Change flow - Step 1) ───────────────────────────
db.notification_templates.insertOne({
  _id: UUID("a1b2c3d4-e5f6-7890-abcd-ef1234567891"),
  code: "account-verify-current-email",
  name: "Verify Current Email OTP Template",
  variables: ["otpCode"],
  titleTemplate: "[FurniSight] Verify Your Current Email",
  bodyTemplate: `<!DOCTYPE html>
<html>
<head>
<style>
    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
    .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
    .header { text-align: center; border-bottom: 2px solid #ee4d2d; padding-bottom: 10px; margin-bottom: 20px; }
    .header h2 { color: #ee4d2d; margin: 0; text-transform: uppercase; font-size: 24px; }
    .content { font-size: 16px; color: #333333; line-height: 1.5; }
    .verification-code { display: block; margin: 30px 0; font-size: 32px; font-weight: bold; color: #ee4d2d; text-align: center; letter-spacing: 5px; background: #fff5f5; padding: 15px; border-radius: 4px; border: 1px dashed #ee4d2d; }
    .footer { font-size: 14px; color: #777777; text-align: center; border-top: 1px solid #eaeaea; padding-top: 10px; margin-top: 20px; }
</style>
</head>
<body>
<div class="container">
    <div class="header">
        <h2>FurniSight</h2>
    </div>
    <div class="content">
        <p>Hello,</p>
        <p>You have initiated a contact change request. Please use the following OTP to verify your <strong>current email address</strong>:</p>
        <span class="verification-code">{{otpCode}}</span>
        <p>This code is valid for 5 minutes. If you did not request this, please ignore this email.</p>
    </div>
    <div class="footer">
        <p>&copy; FurniSight. All rights reserved.</p>
    </div>
</div>
</body>
</html>`,
  type: "SYSTEM",
  channel: "EMAIL",
  defaultImage: "",
  defaultActionUrl: "",
  createdAt: new Date(),
  updatedAt: new Date(),
  version: NumberLong(0)
});

// ─── VERIFY CURRENT PHONE (Change flow - Step 1 via SMS) ───────────────────
db.notification_templates.insertOne({
  _id: UUID("b2c3d4e5-f6a7-8901-bcde-f12345678902"),
  code: "account-verify-current-phone",
  name: "Verify Current Phone OTP Template",
  variables: ["otpCode"],
  titleTemplate: "Your Verification OTP",
  bodyTemplate: "[FurniSight] Your OTP to verify your current phone number is: {{otpCode}}. Valid for 5 minutes. Do not share this code.",
  type: "SYSTEM",
  channel: "SMS",
  defaultImage: "",
  defaultActionUrl: "",
  createdAt: new Date(),
  updatedAt: new Date(),
  version: NumberLong(0)
});

// ─── PHONE CHANGE (Step 2 - OTP to new phone via SMS) ─────────────────────
db.notification_templates.insertOne({
  _id: UUID("c3d4e5f6-a7b8-9012-cdef-123456789003"),
  code: "account-phone-change",
  name: "Phone Change OTP Template",
  variables: ["otpCode"],
  titleTemplate: "Your Phone Change OTP",
  bodyTemplate: "[FurniSight] Your OTP to confirm your new phone number is: {{otpCode}}. Valid for 5 minutes. Do not share this code.",
  type: "SYSTEM",
  channel: "SMS",
  defaultImage: "",
  defaultActionUrl: "",
  createdAt: new Date(),
  updatedAt: new Date(),
  version: NumberLong(0)
});

// ─── EMAIL LINK (Link flow - OTP to new email) ────────────────────────────
db.notification_templates.insertOne({
  _id: UUID("d4e5f6a7-b8c9-0123-def0-234567890104"),
  code: "account-email-link",
  name: "Email Link OTP Template",
  variables: ["otpCode"],
  titleTemplate: "[FurniSight] Link Your Email Address",
  bodyTemplate: `<!DOCTYPE html>
<html>
<head>
<style>
    body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }
    .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; padding: 20px; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
    .header { text-align: center; border-bottom: 2px solid #ee4d2d; padding-bottom: 10px; margin-bottom: 20px; }
    .header h2 { color: #ee4d2d; margin: 0; text-transform: uppercase; font-size: 24px; }
    .content { font-size: 16px; color: #333333; line-height: 1.5; }
    .verification-code { display: block; margin: 30px 0; font-size: 32px; font-weight: bold; color: #ee4d2d; text-align: center; letter-spacing: 5px; background: #fff5f5; padding: 15px; border-radius: 4px; border: 1px dashed #ee4d2d; }
    .footer { font-size: 14px; color: #777777; text-align: center; border-top: 1px solid #eaeaea; padding-top: 10px; margin-top: 20px; }
</style>
</head>
<body>
<div class="container">
    <div class="header">
        <h2>FurniSight</h2>
    </div>
    <div class="content">
        <p>Hello,</p>
        <p>You have requested to link this email address to your account. Please use the following OTP to confirm:</p>
        <span class="verification-code">{{otpCode}}</span>
        <p>This code is valid for 5 minutes. If you did not request this, please ignore this email.</p>
    </div>
    <div class="footer">
        <p>&copy; FurniSight. All rights reserved.</p>
    </div>
</div>
</body>
</html>`,
  type: "SYSTEM",
  channel: "EMAIL",
  defaultImage: "",
  defaultActionUrl: "",
  createdAt: new Date(),
  updatedAt: new Date(),
  version: NumberLong(0)
});

// ─── PHONE LINK (Link flow - OTP to new phone via SMS) ────────────────────
db.notification_templates.insertOne({
  _id: UUID("e5f6a7b8-c9d0-1234-ef01-345678901205"),
  code: "account-phone-link",
  name: "Phone Link OTP Template",
  variables: ["otpCode"],
  titleTemplate: "Link Your Phone Number",
  bodyTemplate: "[FurniSight] Your OTP to link your phone number is: {{otpCode}}. Valid for 5 minutes. Do not share this code.",
  type: "SYSTEM",
  channel: "SMS",
  defaultImage: "",
  defaultActionUrl: "",
  createdAt: new Date(),
  updatedAt: new Date(),
  version: NumberLong(0)
});
