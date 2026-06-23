let jsonString = `{"text": "<p>{{voucherName}}</p>"}`;
let voucherName = '"Siêu Sale"';
let replacedString = jsonString.replace(/\{\{voucherName\}\}/g, voucherName);
console.log("Replaced:", replacedString);
try {
  JSON.parse(replacedString);
  console.log("Parse success");
} catch(e) {
  console.log("Parse error:", e.message);
}
