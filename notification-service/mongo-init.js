db = db.getSiblingDB("notification_db");

// ─── TEMPLATES ────────────────────────────────────────────────────────────────

db.notification_templates.insertMany([
  {
    _id: UUID("22222222-2222-2222-2222-222222222222"),
    code: "media-uploaded",
    name: "Media Uploaded Notification",
    variables: ["fileName", "uploaderName"],
    titleTemplate: "New media uploaded",
    bodyTemplate: "Your file {{fileName}} was uploaded successfully by {{uploaderName}}.",
    type: "MEDIA", channel: "IN_APP",
    defaultImage: "", defaultActionUrl: "",
    createdAt: new Date(), updatedAt: new Date(), version: NumberLong(0)
  },
  {
    _id: UUID("7d9e4a3e-1f5b-48af-91cd-b2354cf1af91"),
    code: "account-verify-code",
    name: "Account Verification Email Template",
    variables: ["verifyUrl"],
    titleTemplate: "Verify Your Account",
    bodyTemplate: `<!DOCTYPE html><html><head><meta charset="UTF-8"><style>body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0}.container{max-width:600px;margin:20px auto;background:#fff;padding:20px;border-radius:8px;box-shadow:0 4px 8px rgba(0,0,0,.1)}.header{text-align:center;border-bottom:2px solid #ee4d2d;padding-bottom:10px;margin-bottom:20px}.header h2{color:#ee4d2d;margin:0;text-transform:uppercase;font-size:24px}.content{font-size:16px;color:#333;line-height:1.5;text-align:center}.button{display:inline-block;margin:30px 0;padding:12px 24px;font-size:16px;color:#fff!important;background:#ee4d2d;text-decoration:none;border-radius:5px;font-weight:700}.footer{font-size:14px;color:#777;text-align:center;border-top:1px solid #eaeaea;padding-top:10px;margin-top:20px}</style></head><body><div class="container"><div class="header"><h2>Account Service</h2></div><div class="content"><p>Hello,</p><p>Please click the button below to verify your account:</p><a href="{{verifyUrl}}" class="button">Verify Account</a><p>If the button doesn't work, copy and paste this link into your browser:</p><p><a href="{{verifyUrl}}">{{verifyUrl}}</a></p><p>If you did not request this, please ignore this email.</p></div><div class="footer"><p>&copy; Account Service. All rights reserved.</p></div></div></body></html>`,
    type: "SYSTEM", channel: "EMAIL",
    defaultImage: "", defaultActionUrl: "",
    createdAt: new Date(), updatedAt: new Date(), version: NumberLong(0)
  },
  {
    _id: UUID("8a9c3d4f-5e6b-7c8d-9e0f-1a2b3c4d5e6f"),
    code: "account-reset-password",
    name: "Account Reset Password Email Template",
    variables: ["token"],
    titleTemplate: "[Account Service] Your Password Reset Code",
    bodyTemplate: `<!DOCTYPE html><html><head><style>body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0}.container{max-width:600px;margin:20px auto;background:#fff;padding:20px;border-radius:8px;box-shadow:0 4px 8px rgba(0,0,0,.1)}.header{text-align:center;border-bottom:2px solid #ee4d2d;padding-bottom:10px;margin-bottom:20px}.header h2{color:#ee4d2d;margin:0;text-transform:uppercase;font-size:24px}.content{font-size:16px;color:#333;line-height:1.5}.verification-code{display:block;margin:30px 0;font-size:32px;font-weight:700;color:#ee4d2d;text-align:center;letter-spacing:5px;background:#fff5f5;padding:15px;border-radius:4px;border:1px dashed #ee4d2d}.footer{font-size:14px;color:#777;text-align:center;border-top:1px solid #eaeaea;padding-top:10px;margin-top:20px}</style></head><body><div class="container"><div class="header"><h2>FurniSight</h2></div><div class="content"><p>Hello,</p><p>We received a request to reset your password. Please use the following code to proceed:</p><span class="verification-code">{{token}}</span><p>If you did not request this code, please safely ignore this email.</p></div><div class="footer"><p>&copy; Account Service. All rights reserved.</p></div></div></body></html>`,
    type: "SYSTEM", channel: "EMAIL",
    defaultImage: "", defaultActionUrl: "",
    createdAt: new Date(), updatedAt: new Date(), version: NumberLong(0)
  },
  {
    _id: UUID("1b2c3d4e-5f6a-7b8c-9d0e-1f2a3b4c5d6e"),
    code: "account-email-change",
    name: "Account Email Change OTP Template",
    variables: ["otpCode"],
    titleTemplate: "[FurniSight] Your Email Change OTP",
    bodyTemplate: `<!DOCTYPE html><html><head><style>body{font-family:Arial,sans-serif;background:#f4f4f4}.container{max-width:600px;margin:20px auto;background:#fff;padding:20px;border-radius:8px}.header{text-align:center;border-bottom:2px solid #ee4d2d;padding-bottom:10px;margin-bottom:20px}.header h2{color:#ee4d2d;margin:0;text-transform:uppercase;font-size:24px}.content{font-size:16px;color:#333;line-height:1.5}.verification-code{display:block;margin:30px 0;font-size:32px;font-weight:700;color:#ee4d2d;text-align:center;letter-spacing:5px;background:#fff5f5;padding:15px;border-radius:4px;border:1px dashed #ee4d2d}.footer{font-size:14px;color:#777;text-align:center;border-top:1px solid #eaeaea;padding-top:10px;margin-top:20px}</style></head><body><div class="container"><div class="header"><h2>FurniSight</h2></div><div class="content"><p>Hello,</p><p>We received a request to change your email address. Please use the following OTP to proceed:</p><span class="verification-code">{{otpCode}}</span><p>If you did not request this code, please safely ignore this email.</p></div><div class="footer"><p>&copy; FurniSight. All rights reserved.</p></div></div></body></html>`,
    type: "SYSTEM", channel: "EMAIL",
    defaultImage: "", defaultActionUrl: "",
    createdAt: new Date(), updatedAt: new Date(), version: NumberLong(0)
  },
  {
    _id: UUID("a1b2c3d4-e5f6-7890-abcd-ef1234567891"),
    code: "account-verify-current-email",
    name: "Verify Current Email OTP Template",
    variables: ["otpCode"],
    titleTemplate: "[FurniSight] Verify Your Current Email",
    bodyTemplate: `<!DOCTYPE html><html><head><style>body{font-family:Arial,sans-serif;background:#f4f4f4}.container{max-width:600px;margin:20px auto;background:#fff;padding:20px;border-radius:8px}.header{text-align:center;border-bottom:2px solid #ee4d2d;padding-bottom:10px;margin-bottom:20px}.header h2{color:#ee4d2d;margin:0;text-transform:uppercase;font-size:24px}.content{font-size:16px;color:#333;line-height:1.5}.verification-code{display:block;margin:30px 0;font-size:32px;font-weight:700;color:#ee4d2d;text-align:center;letter-spacing:5px;background:#fff5f5;padding:15px;border-radius:4px;border:1px dashed #ee4d2d}.footer{font-size:14px;color:#777;text-align:center;border-top:1px solid #eaeaea;padding-top:10px;margin-top:20px}</style></head><body><div class="container"><div class="header"><h2>FurniSight</h2></div><div class="content"><p>Hello,</p><p>You have initiated a contact change request. Please use the following OTP to verify your <strong>current email address</strong>:</p><span class="verification-code">{{otpCode}}</span><p>This code is valid for 5 minutes. If you did not request this, please ignore this email.</p></div><div class="footer"><p>&copy; FurniSight. All rights reserved.</p></div></div></body></html>`,
    type: "SYSTEM", channel: "EMAIL",
    defaultImage: "", defaultActionUrl: "",
    createdAt: new Date(), updatedAt: new Date(), version: NumberLong(0)
  },
  {
    _id: UUID("b2c3d4e5-f6a7-8901-bcde-f12345678902"),
    code: "account-verify-current-phone",
    name: "Verify Current Phone OTP Template",
    variables: ["otpCode"],
    titleTemplate: "Your Verification OTP",
    bodyTemplate: "[FurniSight] Your OTP to verify your current phone number is: {{otpCode}}. Valid for 5 minutes. Do not share this code.",
    type: "SYSTEM", channel: "SMS",
    defaultImage: "", defaultActionUrl: "",
    createdAt: new Date(), updatedAt: new Date(), version: NumberLong(0)
  },
  {
    _id: UUID("c3d4e5f6-a7b8-9012-cdef-123456789003"),
    code: "account-phone-change",
    name: "Phone Change OTP Template",
    variables: ["otpCode"],
    titleTemplate: "Your Phone Change OTP",
    bodyTemplate: "[FurniSight] Your OTP to confirm your new phone number is: {{otpCode}}. Valid for 5 minutes. Do not share this code.",
    type: "SYSTEM", channel: "SMS",
    defaultImage: "", defaultActionUrl: "",
    createdAt: new Date(), updatedAt: new Date(), version: NumberLong(0)
  },
  {
    _id: UUID("d4e5f6a7-b8c9-0123-def0-234567890104"),
    code: "account-email-link",
    name: "Email Link OTP Template",
    variables: ["otpCode"],
    titleTemplate: "[FurniSight] Link Your Email Address",
    bodyTemplate: `<!DOCTYPE html><html><head><style>body{font-family:Arial,sans-serif;background:#f4f4f4}.container{max-width:600px;margin:20px auto;background:#fff;padding:20px;border-radius:8px}.header{text-align:center;border-bottom:2px solid #ee4d2d;padding-bottom:10px;margin-bottom:20px}.header h2{color:#ee4d2d;margin:0;text-transform:uppercase;font-size:24px}.content{font-size:16px;color:#333;line-height:1.5}.verification-code{display:block;margin:30px 0;font-size:32px;font-weight:700;color:#ee4d2d;text-align:center;letter-spacing:5px;background:#fff5f5;padding:15px;border-radius:4px;border:1px dashed #ee4d2d}.footer{font-size:14px;color:#777;text-align:center;border-top:1px solid #eaeaea;padding-top:10px;margin-top:20px}</style></head><body><div class="container"><div class="header"><h2>FurniSight</h2></div><div class="content"><p>Hello,</p><p>You have requested to link this email address to your account. Please use the following OTP to confirm:</p><span class="verification-code">{{otpCode}}</span><p>This code is valid for 5 minutes. If you did not request this, please ignore this email.</p></div><div class="footer"><p>&copy; FurniSight. All rights reserved.</p></div></div></body></html>`,
    type: "SYSTEM", channel: "EMAIL",
    defaultImage: "", defaultActionUrl: "",
    createdAt: new Date(), updatedAt: new Date(), version: NumberLong(0)
  },
  {
    _id: UUID("e5f6a7b8-c9d0-1234-ef01-345678901205"),
    code: "account-phone-link",
    name: "Phone Link OTP Template",
    variables: ["otpCode"],
    titleTemplate: "Link Your Phone Number",
    bodyTemplate: "[FurniSight] Your OTP to link your phone number is: {{otpCode}}. Valid for 5 minutes. Do not share this code.",
    type: "SYSTEM", channel: "SMS",
    defaultImage: "", defaultActionUrl: "",
    createdAt: new Date(), updatedAt: new Date(), version: NumberLong(0)
  },
  {
    _id: UUID("f1a2b3c4-d5e6-7890-abcd-ef0123456789"),
    code: "review-approved",
    name: "Review Approved Notification",
    variables: ["productName", "rating"],
    titleTemplate: "Đánh giá của bạn đã được duyệt! ✅",
    bodyTemplate: "Cảm ơn bạn đã đánh giá sản phẩm '{{productName}}' với {{rating}} sao. Đánh giá của bạn đã được hiển thị công khai.",
    type: "REVIEW", channel: "IN_APP",
    defaultImage: "", defaultActionUrl: "",
    createdAt: new Date(), updatedAt: new Date(), version: NumberLong(0)
  },
  {
    _id: UUID("a1b2c3d4-e5f6-7890-abcd-ef0123456790"),
    code: "new-review-received",
    name: "New Review Received Notification",
    variables: ["productName", "reviewerName", "rating"],
    titleTemplate: "Sản phẩm của bạn vừa có đánh giá mới! ⭐",
    bodyTemplate: "{{reviewerName}} vừa để lại đánh giá {{rating}} sao cho sản phẩm '{{productName}}'. Xem ngay để phản hồi kịp thời.",
    type: "REVIEW", channel: "IN_APP",
    defaultImage: "", defaultActionUrl: "",
    createdAt: new Date(), updatedAt: new Date(), version: NumberLong(0)
  }
]);

// ─── INBOX MESSAGES ───────────────────────────────────────────────────────────

const devUserIds = [
  "52379d96-5238-4fd9-8383-bae82736bb3b", // minhhien
  "f85b5fd8-d60e-4c7e-87ae-5912796d668e", // admin
  "4b33e5c1-cae1-458d-b4b1-e568ddd766f6", // user01
  "7c22e6d3-1111-4aab-b999-aabbcc001122", // user02
  "8d33f7e4-2222-4bbc-caaa-bbccdd002233", // user03
];

const now = new Date();
const h  = 60 * 60 * 1000;
const d  = 24 * h;
const BASE_CLASS = "com.furnisight.notification.domain.model.entity.InboxMessage";

devUserIds.forEach(uid => {
  const userId = UUID(uid);

  const messages = [
    {
      title: "Đơn hàng đã đặt thành công 🛒",
      body: "Đơn hàng #FS-98402 của bạn đã được xác nhận thành công và đang được chuẩn bị đóng gói.",
      image: "https://images.unsplash.com/photo-1540518614846-7eded433c457?q=80&w=200&auto=format&fit=crop",
      type: "ORDER", read: false, readAt: null,
      createdAt: new Date(now - 2 * h)
    },
    {
      title: "Mã giảm giá 20% đặc biệt dành cho bạn! 🎁",
      body: "Chào mừng bạn đến với FurniSight! Nhập mã 'WELCOME20' để được giảm giá 20% cho đơn hàng tiếp theo.",
      image: "https://images.unsplash.com/photo-1513519245088-0e12902e5a38?q=80&w=200&auto=format&fit=crop",
      type: "PROMOTION", read: false, readAt: null,
      createdAt: new Date(now - 1 * d)
    },
    {
      title: "Xác thực tài khoản thành công! 🛡️",
      body: "Tài khoản của bạn đã được xác thực thành công. Bắt đầu trải nghiệm thiết kế phòng 3D tuyệt vời ngay hôm nay!",
      image: "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?q=80&w=200&auto=format&fit=crop",
      type: "SYSTEM", read: true,
      readAt: new Date(now - 2 * d + h),
      createdAt: new Date(now - 2 * d)
    },
    {
      title: "Bản thiết kế của bạn được yêu thích! ❤️",
      body: "Thiết kế 'Phòng khách Bắc Âu tối giản' của bạn đã nhận được hơn 50 lượt thích từ cộng đồng.",
      image: "https://images.unsplash.com/photo-1616486338812-3dadae4b4ace?q=80&w=200&auto=format&fit=crop",
      type: "SOCIAL", read: false, readAt: null,
      createdAt: new Date(now - 3 * d)
    },
    {
      title: "Đánh giá của bạn đã được duyệt! ✅",
      body: "Cảm ơn bạn đã đánh giá sản phẩm 'Modern Leather Sofa' với 5 sao. Đánh giá của bạn đã được hiển thị công khai.",
      image: "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?q=80&w=200&auto=format&fit=crop",
      type: "REVIEW", read: false, readAt: null,
      createdAt: new Date(now - 1 * h)
    },
    {
      title: "🔥 Flash Sale 48h — Giảm đến 40% nội thất cao cấp!",
      body: "Chỉ trong 48 giờ! Hàng trăm sản phẩm nội thất giảm giá sâu. Đừng bỏ lỡ cơ hội sở hữu bộ sofa da mơ ước với giá ưu đãi.",
      image: "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?q=80&w=200&auto=format&fit=crop",
      type: "PROMOTION", read: false, readAt: null,
      createdAt: new Date(now - 30 * 60 * 1000)
    }
  ];

  messages.forEach(m => {
    db.inbox_messages.insertOne({
      _id: UUID(),
      _class: BASE_CLASS,
      userId,
      title: m.title,
      body: m.body,
      image: m.image,
      actionUrl: "",
      type: m.type,
      read: m.read,
      deleted: false,
      readAt: m.readAt,
      deletedAt: null,
      expireAt: new Date(now.getTime() + 30 * d),
      createdAt: m.createdAt,
      updatedAt: m.createdAt,
      version: NumberLong(0)
    });
  });
});

print("✅ mongo-init.js completed: templates + inbox messages inserted.");
