/**
 * SupportDesk - Core Frontend Application Framework
 */

const API = {
    async request(url, options = {}) {
        const defaultHeaders = {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        };

        // Extract CSRF token from cookie if available
        const csrfCookie = document.cookie
            .split('; ')
            .find(row => row.startsWith('XSRF-TOKEN='));
        if (csrfCookie) {
            defaultHeaders['X-XSRF-TOKEN'] = decodeURIComponent(csrfCookie.split('=')[1]);
        }

        const mergedOptions = {
            ...options,
            headers: {
                ...defaultHeaders,
                ...(options.headers || {})
            },
            credentials: 'same-origin'
        };

        try {
            const response = await fetch(url, mergedOptions);

            if (response.status === 401) {
                window.location.href = '/login?expired=true';
                return null;
            }

            if (response.status === 403) {
                showToast('Access denied: You do not have permission for this action', 'danger');
                return null;
            }

            if (response.status === 204) {
                return null;
            }

            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('text/csv')) {
                return await response.text();
            }

            let data = null;
            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else {
                const text = await response.text();
                try {
                    data = JSON.parse(text);
                } catch (e) {
                    data = text;
                }
            }

            if (!response.ok) {
                const errorMessage = (data && data.message) ? data.message : `Request failed (HTTP ${response.status})`;
                throw new Error(errorMessage);
            }

            return data;
        } catch (error) {
            console.error('API Error:', error);
            showToast(error.message, 'danger');
            throw error;
        }
    },

    get(url) {
        return this.request(url, { method: 'GET' });
    },

    post(url, data) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    put(url, data) {
        return this.request(url, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    delete(url) {
        return this.request(url, { method: 'DELETE' });
    }
};

// Toast notification helper
function showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        container.className = 'toast-container position-fixed top-0 end-0 p-3';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
    }

    const toastEl = document.createElement('div');
    toastEl.className = `toast align-items-center text-white bg-${type === 'error' ? 'danger' : type} border-0 show shadow-lg mb-2`;
    toastEl.setAttribute('role', 'alert');
    toastEl.setAttribute('aria-live', 'assertive');
    toastEl.setAttribute('aria-atomic', 'true');

    toastEl.innerHTML = `
        <div class="d-flex">
            <div class="toast-body">
                <i class="bi bi-${type === 'success' ? 'check-circle' : type === 'danger' ? 'exclamation-triangle' : 'info-circle'} me-2"></i>
                ${escapeHtml(message)}
            </div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close" onclick="this.closest('.toast').remove()"></button>
        </div>
    `;

    container.appendChild(toastEl);
    setTimeout(() => {
        if (toastEl.parentNode) {
            toastEl.remove();
        }
    }, 4500);
}

// Status badge helper
function getStatusBadge(status) {
    if (!status) return '<span class="badge bg-secondary">Unknown</span>';
    const s = status.toUpperCase();
    switch (s) {
        case 'OPEN':
            return '<span class="badge bg-primary"><i class="bi bi-circle me-1"></i>Open</span>';
        case 'IN_PROGRESS':
            return '<span class="badge bg-warning text-dark"><i class="bi bi-hourglass-split me-1"></i>In Progress</span>';
        case 'WAITING_FOR_CUSTOMER':
            return '<span class="badge bg-info text-dark"><i class="bi bi-pause-circle me-1"></i>Waiting for Customer</span>';
        case 'RESOLVED':
            return '<span class="badge bg-success"><i class="bi bi-check-circle me-1"></i>Resolved</span>';
        case 'CLOSED':
            return '<span class="badge bg-secondary"><i class="bi bi-archive me-1"></i>Closed</span>';
        default:
            return `<span class="badge bg-light text-dark">${escapeHtml(status)}</span>`;
    }
}

// Priority badge helper
function getPriorityBadge(priority) {
    if (!priority) return '<span class="badge bg-secondary">None</span>';
    const p = priority.toUpperCase();
    switch (p) {
        case 'URGENT':
            return '<span class="badge bg-danger"><i class="bi bi-fire me-1"></i>URGENT</span>';
        case 'HIGH':
            return '<span class="badge bg-warning text-dark"><i class="bi bi-arrow-up-circle me-1"></i>HIGH</span>';
        case 'MEDIUM':
            return '<span class="badge bg-primary"><i class="bi bi-dash-circle me-1"></i>MEDIUM</span>';
        case 'LOW':
            return '<span class="badge bg-info text-dark"><i class="bi bi-arrow-down-circle me-1"></i>LOW</span>';
        default:
            return `<span class="badge bg-secondary">${escapeHtml(priority)}</span>`;
    }
}

// Format SLA remaining indicator
function formatSlaRemaining(minutes, breached = false) {
    if (breached) {
        return '<span class="badge bg-danger"><i class="bi bi-exclamation-octagon me-1"></i>Breached</span>';
    }
    if (minutes === null || minutes === undefined) {
        return '<span class="text-muted">—</span>';
    }
    if (minutes <= 0) {
        return '<span class="badge bg-danger"><i class="bi bi-exclamation-circle me-1"></i>Overdue</span>';
    }
    if (minutes < 60) {
        return `<span class="badge bg-warning text-dark"><i class="bi bi-clock me-1"></i>${minutes}m remaining</span>`;
    }
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours < 24) {
        return `<span class="badge bg-info text-dark"><i class="bi bi-clock me-1"></i>${hours}h ${mins}m</span>`;
    }
    const days = Math.floor(hours / 24);
    return `<span class="badge bg-light text-dark border"><i class="bi bi-calendar-event me-1"></i>${days}d ${hours % 24}h</span>`;
}

// Format date nicely
function formatDate(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    return d.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Format short date (YYYY-MM-DD)
function formatDateShort(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return dateStr;
    return d.toISOString().split('T')[0];
}

// Relative time string (e.g., "5 minutes ago")
function timeAgo(dateStr) {
    if (!dateStr) return '—';
    const date = new Date(dateStr);
    const now = new Date();
    const seconds = Math.floor((now - date) / 1000);

    if (seconds < 60) return 'Just now';
    const minutes = Math.floor(seconds / 60);
    if (minutes < 60) return `${minutes}m ago`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours}h ago`;
    const days = Math.floor(hours / 24);
    if (days < 30) return `${days}d ago`;
    return formatDateShort(dateStr);
}

// Helper to escape HTML and prevent XSS
function escapeHtml(text) {
    if (text === null || text === undefined) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Load current user profile into header
async function loadCurrentUser() {
    try {
        const user = await API.get('/api/auth/me');
        if (user) {
            const userNameElements = document.querySelectorAll('.current-user-name');
            userNameElements.forEach(el => el.textContent = user.fullName || user.email);

            const userRoleElements = document.querySelectorAll('.current-user-role');
            userRoleElements.forEach(el => el.textContent = user.role || '');

            const userOrgElements = document.querySelectorAll('.current-user-org');
            userOrgElements.forEach(el => el.textContent = user.organizationName || '');

            return user;
        }
    } catch (e) {
        console.warn('Could not load current user:', e);
    }
    return null;
}

// Render reusable pagination controls
function renderPagination(pageDTO, containerId, onPageChange) {
    const container = document.getElementById(containerId);
    if (!container || !pageDTO) return;

    if (pageDTO.totalPages <= 1) {
        container.innerHTML = `<span class="text-muted small">Showing all ${pageDTO.totalElements} records</span>`;
        return;
    }

    const current = pageDTO.page;
    const totalPages = pageDTO.totalPages;

    let html = `
        <div class="d-flex justify-content-between align-items-center w-100 flex-wrap gap-2">
            <span class="text-muted small">Page ${current + 1} of ${totalPages} (${pageDTO.totalElements} total)</span>
            <ul class="pagination pagination-sm mb-0">
                <li class="page-item ${current === 0 ? 'disabled' : ''}">
                    <button class="page-link" onclick="${onPageChange}(${current - 1})" aria-label="Previous">&laquo;</button>
                </li>
    `;

    const startPage = Math.max(0, current - 2);
    const endPage = Math.min(totalPages - 1, current + 2);

    for (let i = startPage; i <= endPage; i++) {
        html += `
            <li class="page-item ${i === current ? 'active' : ''}">
                <button class="page-link" onclick="${onPageChange}(${i})">${i + 1}</button>
            </li>
        `;
    }

    html += `
                <li class="page-item ${current >= totalPages - 1 ? 'disabled' : ''}">
                    <button class="page-link" onclick="${onPageChange}(${current + 1})" aria-label="Next">&raquo;</button>
                </li>
            </ul>
        </div>
    `;

    container.innerHTML = html;
}

