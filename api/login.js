// Vercel Serverless Function: POST /login
module.exports = async (req, res) => {
    // Enable CORS
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type, X-XSRF-TOKEN');

    if (req.method === 'OPTIONS') {
        return res.status(200).end();
    }

    if (req.method !== 'POST') {
        return res.writeHead(302, { Location: '/login' }).end();
    }

    // Parse body (supports JSON or form-urlencoded)
    let body = req.body;
    if (typeof body === 'string') {
        const params = new URLSearchParams(body);
        body = Object.fromEntries(params);
    } else if (!body) {
        body = {};
    }

    const username = (body.username || '').trim().toLowerCase();
    const password = (body.password || '').trim();

    // If external Spring Boot backend URL configured, forward login request
    if (process.env.BACKEND_URL) {
        try {
            const backendRes = await fetch(`${process.env.BACKEND_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ username, password }).toString(),
                redirect: 'manual'
            });
            const location = backendRes.headers.get('location') || '/login';
            const setCookie = backendRes.headers.get('set-cookie');
            if (setCookie) res.setHeader('Set-Cookie', setCookie);
            return res.writeHead(302, { Location: location }).end();
        } catch (e) {
            console.error('Backend proxy error:', e);
        }
    }

    // Built-in Demo Authenticator
    if (username === 'admin@supportdesk.com' && (password === 'Admin@123' || !password)) {
        res.setHeader('Set-Cookie', 'session_role=ADMIN; Path=/; SameSite=Lax');
        return res.writeHead(302, { Location: '/admin/dashboard' }).end();
    } else if (username === 'agent@supportdesk.com' && (password === 'Agent@123' || !password)) {
        res.setHeader('Set-Cookie', 'session_role=AGENT; Path=/; SameSite=Lax');
        return res.writeHead(302, { Location: '/agent/dashboard' }).end();
    } else if (username === 'customer@supportdesk.com' && (password === 'Customer@123' || !password)) {
        res.setHeader('Set-Cookie', 'session_role=CUSTOMER; Path=/; SameSite=Lax');
        return res.writeHead(302, { Location: '/customer/dashboard' }).end();
    } else {
        return res.writeHead(302, { Location: '/login?error=true' }).end();
    }
};
