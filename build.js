const fs = require('fs');
const path = require('path');

const srcDir = path.join(__dirname, 'supportdesk', 'src', 'main', 'resources', 'static');
const destDir = path.join(__dirname, 'public');

function copyRecursive(src, dest) {
    if (!fs.existsSync(src)) return;
    if (!fs.existsSync(dest)) {
        fs.mkdirSync(dest, { recursive: true });
    }
    const entries = fs.readdirSync(src, { withFileTypes: true });
    for (const entry of entries) {
        const srcPath = path.join(src, entry.name);
        const destPath = path.join(dest, entry.name);
        if (entry.isDirectory()) {
            copyRecursive(srcPath, destPath);
        } else {
            fs.copyFileSync(srcPath, destPath);
        }
    }
}

console.log('Copying static assets from', srcDir, 'to', destDir);
copyRecursive(srcDir, destDir);

// Ensure index.html exists in public root (redirecting to /login)
const indexHtml = `<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="refresh" content="0; url=/login">
    <title>SupportDesk</title>
</head>
<body>
    <p>Redirecting to <a href="/login">Login</a>...</p>
</body>
</html>`;

fs.writeFileSync(path.join(destDir, 'index.html'), indexHtml, 'utf8');
console.log('Build complete! Public directory is ready for Vercel deployment.');
