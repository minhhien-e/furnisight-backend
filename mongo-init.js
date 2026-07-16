db = db.getSiblingDB("notification_db");
// ─── TEMPLATES ────────────────────────────────────────────────────────────────

db.notification_templates.insertMany([
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
      _id: UUID("33333333-3333-3333-3333-333333333333"),
      code: "order-placed",
      name: "Order Placed Email Template",
      variables: ["orderCode", "totalAmount"],
      titleTemplate: "[FurniSight] Đặt hàng thành công - Đơn hàng #{{orderCode}}",
      bodyTemplate: `<!DOCTYPE html><html><head><meta charset="UTF-8"><style>body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0}.container{max-width:600px;margin:20px auto;background:#fff;padding:20px;border-radius:8px;box-shadow:0 4px 8px rgba(0,0,0,.1)}.header{text-align:center;border-bottom:2px solid #ee4d2d;padding-bottom:10px;margin-bottom:20px}.header h2{color:#ee4d2d;margin:0;text-transform:uppercase;font-size:24px}.content{font-size:16px;color:#333;line-height:1.5}.footer{font-size:14px;color:#777;text-align:center;border-top:1px solid #eaeaea;padding-top:10px;margin-top:20px}</style></head><body><div class="container"><div class="header"><h2>FurniSight</h2></div><div class="content"><p>Xin chào,</p><p>Cảm ơn bạn đã đặt hàng tại FurniSight. Đơn hàng <strong>#{{orderCode}}</strong> của bạn đã được tiếp nhận thành công.</p><p>Tổng số tiền thanh toán: <strong>{{totalAmount}} VND</strong></p><p>Chúng tôi sẽ xử lý đơn hàng và giao hàng trong thời gian sớm nhất.</p></div><div class="footer"><p>&copy; FurniSight. All rights reserved.</p></div></div></body></html>`,
      type: "SYSTEM", channel: "EMAIL",
      defaultImage: "", defaultActionUrl: "",
      createdAt: new Date(), updatedAt: new Date(), version: NumberLong(0)
    },
  {
      _id: UUID("c3b4d5e6-f7a8-9b0c-1d2e-3f4a5b6c7d8e"),
      code: "account-created",
      name: "Account Created Email Template",
      variables: [],
      titleTemplate: "[FurniSight] Chào mừng bạn đến với FurniSight",
      bodyTemplate: `<!DOCTYPE html><html><head><style>body{font-family:Arial,sans-serif;background:#f4f4f4}.container{max-width:600px;margin:20px auto;background:#fff;padding:20px;border-radius:8px}.header{text-align:center;border-bottom:2px solid #ee4d2d;padding-bottom:10px;margin-bottom:20px}.header h2{color:#ee4d2d;margin:0;text-transform:uppercase;font-size:24px}.content{font-size:16px;color:#333;line-height:1.5}.footer{font-size:14px;color:#777;text-align:center;border-top:1px solid #eaeaea;padding-top:10px;margin-top:20px}</style></head><body><div class="container"><div class="header"><h2>FurniSight</h2></div><div class="content"><p>Xin chào,</p><p>Chào mừng bạn gia nhập cộng đồng FurniSight! Tài khoản của bạn đã được khởi tạo thành công.</p><p>Hãy bắt đầu khám phá và trải nghiệm mua sắm nội thất tuyệt vời cùng chúng tôi.</p></div><div class="footer"><p>&copy; FurniSight. All rights reserved.</p></div></div></body></html>`,
      type: "SYSTEM", channel: "EMAIL",
      defaultImage: "", defaultActionUrl: "",
      createdAt: new Date(), updatedAt: new Date(), version: NumberLong(0)
    },
  {
      _id: UUID("e7f8a9b0-c1d2-e3f4-a5b6-c7d8e9f0a1b2"),
      code: "order-status-changed",
      name: "Order Status Changed Email Template",
      variables: ["orderCode", "nextStatus", "paymentMethod"],
      titleTemplate: "[FurniSight] Cập nhật trạng thái đơn hàng #{{orderCode}}",
      bodyTemplate: `<!DOCTYPE html><html><head><meta charset="UTF-8"><style>body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:0}.container{max-width:600px;margin:20px auto;background:#fff;padding:20px;border-radius:8px;box-shadow:0 4px 8px rgba(0,0,0,.1)}.header{text-align:center;border-bottom:2px solid #ee4d2d;padding-bottom:10px;margin-bottom:20px}.header h2{color:#ee4d2d;margin:0;text-transform:uppercase;font-size:24px}.content{font-size:16px;color:#333;line-height:1.5}.footer{font-size:14px;color:#777;text-align:center;border-top:1px solid #eaeaea;padding-top:10px;margin-top:20px}</style></head><body><div class="container"><div class="header"><h2>FurniSight</h2></div><div class="content"><p>Xin chào,</p><p>Đơn hàng <strong>#{{orderCode}}</strong> của bạn vừa được cập nhật trạng thái mới.</p><#if paymentMethod == 'cod'><p>Phương thức thanh toán: Thanh toán khi nhận hàng (COD).</p><#elseif paymentMethod == 'vnpay'><p>Phương thức thanh toán: VNPay.</p></#if><p>Chi tiết đơn hàng bạn có thể kiểm tra trong phần Lịch sử mua hàng của mình.</p></div><div class="footer"><p>&copy; FurniSight. All rights reserved.</p></div></div></body></html>`,
      type: "ORDER", channel: "EMAIL",
      defaultImage: "", defaultActionUrl: "",
      createdAt: new Date(), updatedAt: new Date(), version: NumberLong(0)
    },
  {
      _id: UUID("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d"),
      code: "order-status-changed-push",
      name: "Order Status Changed Push Template",
      variables: ["orderCode", "nextStatus", "paymentMethod"],
      titleTemplate: "Cập nhật đơn hàng #{{orderCode}}",
      bodyTemplate: "<#if nextStatus == 'PAID'>Đơn hàng của bạn đã được thanh toán thành công.<#elseif nextStatus == 'SHIPPING'>Đơn hàng đang trên đường giao đến bạn.<#elseif nextStatus == 'DELIVERED'>Giao hàng thành công. Cảm ơn bạn!<#elseif nextStatus == 'CANCELLED'>Đơn hàng đã bị hủy.<#else>Đơn hàng của bạn vừa chuyển sang trạng thái mới.</#if>",
      type: "ORDER", channel: "IN_APP",
      defaultImage: "", defaultActionUrl: "",
      createdAt: new Date(), updatedAt: new Date(), version: NumberLong(0)
    },
  {
    _id: UUID("2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e"),
    code: "social-account-created",
    name: "Social Account Created Email Template",
    variables: [],
    titleTemplate: "[FurniSight] Đăng nhập thành công với tài khoản liên kết",
    bodyTemplate: `<!DOCTYPE html><html><head><style>body{font-family:Arial,sans-serif;background:#f4f4f4}.container{max-width:600px;margin:20px auto;background:#fff;padding:20px;border-radius:8px}.header{text-align:center;border-bottom:2px solid #ee4d2d;padding-bottom:10px;margin-bottom:20px}.header h2{color:#ee4d2d;margin:0;text-transform:uppercase;font-size:24px}.content{font-size:16px;color:#333;line-height:1.5}.footer{font-size:14px;color:#777;text-align:center;border-top:1px solid #eaeaea;padding-top:10px;margin-top:20px}</style></head><body><div class="container"><div class="header"><h2>FurniSight</h2></div><div class="content"><p>Xin chào,</p><p>Bạn đã đăng nhập thành công vào FurniSight bằng tài khoản mạng xã hội.</p><p>Hãy bắt đầu khám phá và trải nghiệm mua sắm nội thất tuyệt vời cùng chúng tôi.</p></div><div class="footer"><p>&copy; FurniSight. All rights reserved.</p></div></div></body></html>`,
    type: "SYSTEM", channel: "EMAIL",
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
db = db.getSiblingDB("furnisight_cart");

db.createCollection("carts");
print("✅ mongo-init.js completed: templates + inbox messages inserted.");
