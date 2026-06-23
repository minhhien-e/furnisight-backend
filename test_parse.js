const body = `<!-- UNLAYER_DESIGN_START {"key": "value"} UNLAYER_DESIGN_END -->`;
const match = body.match(/<!--\s*UNLAYER_DESIGN_START\s*([\s\S]*?)\s*UNLAYER_DESIGN_END\s*-->/);
console.log(match ? match[1] : "no match");
