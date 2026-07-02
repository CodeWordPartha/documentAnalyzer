// const BASE_URL = 'http://localhost:8081';
const BASE_URL = 'https://document-analyzer-er3v.onrender.com';
let searchMode = 'title'; // 'title' or 'content'

// ==================== AUTH ====================

async function login() {
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    if (!email || !password) {
        showError('errorAlert', 'Please enter email and password');
        return;
    }

    const loginBtn = document.getElementById('loginBtn');
    loginBtn.innerText = 'Logging in...';
    loginBtn.disabled = true;

    try {
        const response = await fetch(`${BASE_URL}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (response.ok) {
            // Save token and user info in localStorage
            localStorage.setItem('token', data.data.token);
            localStorage.setItem('userId', data.data.user.id);
            localStorage.setItem('username', data.data.user.username);

            // Redirect to dashboard
            window.location.href = 'dashboard.html';
        } else {
            showError('errorAlert', data.message || 'Login failed');
        }

    } catch (error) {
        showError('errorAlert', 'Cannot connect to server. Make sure backend is running.');
    } finally {
        loginBtn.innerText = 'Login';
        loginBtn.disabled = false;
    }
}

// ==================== REGISTER ====================

async function register() {
    const username = document.getElementById('username').value.trim();
    const email = document.getElementById('email').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    hideError('errorAlert');
    hideError('successAlert');

    // Basic validation
    if (!username || !email || !phone || !password || !confirmPassword) {
        showError('errorAlert', 'Please fill in all fields');
        return;
    }

    if (!/^[0-9]{10,15}$/.test(phone)) {
    showError('errorAlert', 'Phone number must be 10-15 digits only');
    return;
}

    if (password !== confirmPassword) {
        showError('errorAlert', 'Passwords do not match');
        return;
    }

    if (password.length < 8) {
        showError('errorAlert', 'Password must be at least 6 characters');
        return;
    }

    const registerBtn = document.getElementById('registerBtn');
    registerBtn.innerText = 'Creating account...';
    registerBtn.disabled = true;

    try {
        const response = await fetch(`${BASE_URL}/api/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, phone, password })
        });

        const data = await response.json();

        if (response.ok) {
            const successEl = document.getElementById('successAlert');
            successEl.innerText = 'Account created successfully! Redirecting to login...';
            successEl.classList.remove('d-none');

            setTimeout(() => {
                window.location.href = 'index.html';
            }, 2000);

        } else {
            showError('errorAlert', data.message || 'Registration failed');
        }

    } catch (error) {
        showError('errorAlert', 'Cannot connect to server. Make sure backend is running.');
    } finally {
        registerBtn.innerText = 'Create Account';
        registerBtn.disabled = false;
    }
}

// ==================== HELPERS ====================

function getToken() {
    return localStorage.getItem('token');
}

function getUserId() {
    return localStorage.getItem('userId');
}

function getUsername() {
    return localStorage.getItem('username');
}

function logout() {
    localStorage.clear();
    window.location.href = 'index.html';
}

function showError(elementId, message) {
    const el = document.getElementById(elementId);
    el.innerText = message;
    el.classList.remove('d-none');
}

function hideError(elementId) {
    const el = document.getElementById(elementId);
    el.classList.add('d-none');
}

function authHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${getToken()}`
    };
}

// ==================== DOCUMENTS ====================

async function loadDocuments() {
    try {
        const response = await fetch(`${BASE_URL}/api/document/user/${getUserId()}`, {
    headers: authHeaders()
});

        const data = await response.json();

        if (response.ok) {
            renderDocuments(data.data || data);
        } else if (response.status === 401) {
            logout();
        } else {
            document.getElementById('documentsContainer').innerHTML =
                '<div class="text-center text-danger">Failed to load documents</div>';
        }
    } catch (error) {
        document.getElementById('documentsContainer').innerHTML =
            '<div class="text-center text-danger">Cannot connect to server</div>';
    }
}

function renderDocuments(documents) {
    const container = document.getElementById('documentsContainer');

    if (!documents || documents.length === 0) {
        container.innerHTML = '<div class="text-center text-muted py-4">No documents yet. Upload your first document!</div>';
        return;
    }

    container.innerHTML = documents.map(doc => `
        <div class="card document-card mb-3" onclick="openDocument(${doc.id})">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="mb-1">${doc.title}</h6>
                        <small class="text-muted">${doc.description || 'No description'}</small>
                    </div>
                    <div class="text-end">
                        <small class="text-muted d-block">${doc.fileType || 'Text'}</small>
                        <small class="text-muted">${new Date(doc.createdAt).toLocaleDateString()}</small>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

function openDocument(documentId) {
    localStorage.setItem('currentDocId', documentId);
    window.location.href = 'document.html';
}

async function uploadDocument() {
    const title = document.getElementById('docTitle').value;
    const description = document.getElementById('docDescription').value;
    const file = document.getElementById('docFile').files[0];

    hideError('uploadError');
    hideError('uploadSuccess');

    if (!title || !file) {
        showError('uploadError', 'Please enter a title and select a file');
        return;
    }

    const uploadBtn = document.getElementById('uploadBtn');
    uploadBtn.innerText = 'Uploading...';
    uploadBtn.disabled = true;

    try {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('title', title);
        formData.append('description', description);

        const response = await fetch(
    `${BASE_URL}/api/document/upload?userId=${getUserId()}`,
            {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${getToken()}` },
                body: formData
            }
        );

        const data = await response.json();

        if (response.ok) {
            const successEl = document.getElementById('uploadSuccess');
            successEl.innerText = 'Document uploaded successfully!';
            successEl.classList.remove('d-none');

            // Clear form
            document.getElementById('docTitle').value = '';
            document.getElementById('docDescription').value = '';
            document.getElementById('docFile').value = '';

            // Reload documents list
            loadDocuments();
        } else {
            showError('uploadError', data.message || 'Upload failed');
        }

    } catch (error) {
        showError('uploadError', 'Cannot connect to server');
    } finally {
        uploadBtn.innerText = 'Upload Document';
        uploadBtn.disabled = false;
    }
}

// ==================== DOCUMENT DETAIL ====================

async function loadDocumentDetail() {
    try {
        const response = await fetch(
            `${BASE_URL}/api/document/${documentId}?userId=${getUserId()}`,
            { headers: authHeaders() }
        );

        const data = await response.json();

        if (response.ok) {
            renderDocumentDetail(data);
        } else if (response.status === 401) {
            logout();
        }

    } catch (error) {
        console.error('Failed to load document', error);
    }
}

function renderDocumentDetail(doc) {
    document.getElementById('docTitle').innerText = doc.title;
    document.getElementById('docDescription').innerText = doc.description || '';
    document.getElementById('docMeta').innerText =
        `${doc.fileType || 'Text'} • ${new Date(doc.createdAt).toLocaleDateString()}`;

    // If already analyzed, show AI results
    if (doc.aiSummary) {
        showAiResults(doc);
    }
}

function showAiResults(doc) {
    document.getElementById('aiResults').classList.remove('d-none');
    document.getElementById('aiSummary').innerText = doc.aiSummary;
    document.getElementById('docType').innerText = doc.aiDocumentType || 'Unknown';

    // Sentiment badge color
    const sentiment = (doc.aiSentiment || 'NEUTRAL').toUpperCase();
    const sentimentBadge = document.getElementById('sentimentBadge');
    sentimentBadge.innerText = sentiment;
    sentimentBadge.className = 'badge badge-' + sentiment.toLowerCase();

    // Key topics
    const topicsContainer = document.getElementById('keyTopics');
    if (doc.aiKeyTopics && doc.aiKeyTopics.length > 0) {
        topicsContainer.innerHTML = doc.aiKeyTopics
            .map(topic => `<span class="topic-badge">${topic}</span>`)
            .join('');
    } else {
        topicsContainer.innerHTML = '<small class="text-muted">None found</small>';
    }
}

async function analyzeDocument() {
    const analyzeBtn = document.getElementById('analyzeBtn');
    analyzeBtn.innerText = 'Analyzing...';
    analyzeBtn.disabled = true;

    try {
        const response = await fetch(
            `${BASE_URL}/api/document/${documentId}/analyze?userId=${getUserId()}`,
            {
                method: 'POST',
                headers: authHeaders()
            }
        );

        const data = await response.json();

        if (response.ok) {
            showAiResults(data);
            analyzeBtn.innerText = 'Re-analyze';
        } else if (response.status === 429) {
            alert('Rate limit exceeded. Please wait a minute before analyzing again.');
            analyzeBtn.innerText = 'Analyze with AI';
        } else {
            alert('Analysis failed. Please try again.');
            analyzeBtn.innerText = 'Analyze with AI';
        }

    } catch (error) {
        alert('Cannot connect to server');
        analyzeBtn.innerText = 'Analyze with AI';
    } finally {
        analyzeBtn.disabled = false;
    }
}

async function askQuestion() {
    const question = document.getElementById('questionInput').value.trim();

    if (!question) {
        showError('askError', 'Please enter a question');
        return;
    }

    hideError('askError');

    const askBtn = document.getElementById('askBtn');
    askBtn.innerText = 'Thinking...';
    askBtn.disabled = true;

    try {
        const response = await fetch(
            `${BASE_URL}/api/document/${documentId}/ask?userId=${getUserId()}&question=${encodeURIComponent(question)}`,
            { headers: authHeaders() }
        );

        const data = await response.json();

        if (response.ok) {
            document.getElementById('answerContainer').classList.remove('d-none');
            document.getElementById('answerText').innerText = data.data.answer;
        } else if (response.status === 429) {
            showError('askError', 'Rate limit exceeded. Please wait a minute.');
        } else {
            showError('askError', 'Failed to get answer. Please try again.');
        }

    } catch (error) {
        showError('askError', 'Cannot connect to server');
    } finally {
        askBtn.innerText = 'Ask';
        askBtn.disabled = false;
    }
}

function renderDocuments(documents) {
    const container = document.getElementById('documentsContainer');

    if (!documents || documents.length === 0) {
        container.innerHTML = '<div class="text-center text-muted py-4">No documents yet. Upload your first document!</div>';
        return;
    }

    container.innerHTML = documents.map(doc => `
        <div class="card document-card mb-3">
            <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                    <div onclick="openDocument(${doc.id})" style="cursor:pointer; flex:1">
                        <h6 class="mb-1">${doc.title}</h6>
                        <small class="text-muted">${doc.description || 'No description'}</small>
                    </div>
                    <div class="text-end d-flex align-items-center gap-3">
                        <div>
                            <small class="text-muted d-block">${doc.fileType || 'Text'}</small>
                            <small class="text-muted">${new Date(doc.createdAt).toLocaleDateString()}</small>
                        </div>
                        <button 
                            onclick="deleteDocument(event, ${doc.id})" 
                            class="btn btn-outline-danger btn-sm">
                            Delete
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
}

async function deleteDocument(event, documentId) {
    event.stopPropagation();

    if (!confirm('Are you sure you want to delete this document?')) {
        return;
    }

    try {
        const response = await fetch(
            `${BASE_URL}/api/document/${documentId}?userId=${getUserId()}`,
            {
                method: 'DELETE',
                headers: authHeaders()
            }
        );

        if (response.ok) {
            loadDocuments();
        } else {
            alert('Failed to delete document');
        }

    } catch (error) {
        alert('Cannot connect to server');
    }
}
async function searchDocuments() {
    const keyword = document.getElementById('searchInput').value.trim();

    if (!keyword) {
        loadDocuments();
        return;
    }

    try {
        const response = await fetch(
            `${BASE_URL}/api/document/search?userId=${getUserId()}&keyword=${encodeURIComponent(keyword)}`,
            { headers: authHeaders() }
        );

        const data = await response.json();

        if (response.ok) {
            renderDocuments(data);
        } else {
            alert('Search failed');
        }

    } catch (error) {
        alert('Cannot connect to server');
    }
}

function clearSearch() {
    document.getElementById('searchInput').value = '';
    loadDocuments();
}

function setSearchMode(mode) {
    searchMode = mode;

    const btnTitle = document.getElementById('btnTitle');
    const btnContent = document.getElementById('btnContent');
    const searchInput = document.getElementById('searchInput');
    const searchHint = document.getElementById('searchHint');

    if (mode === 'title') {
        btnTitle.className = 'btn btn-primary btn-sm';
        btnContent.className = 'btn btn-outline-primary btn-sm';
        searchInput.placeholder = 'Search documents by title...';
        searchHint.innerText = 'Searching in PostgreSQL DB by document title';
    } else {
        btnTitle.className = 'btn btn-outline-primary btn-sm';
        btnContent.className = 'btn btn-primary btn-sm';
        searchInput.placeholder = 'Enter keywords separated by comma e.g. finance, revenue, Q3';
        searchHint.innerText = 'Searching in-memory inverted index by document content keywords';
    }
}

async function searchDocuments() {
    const keyword = document.getElementById('searchInput').value.trim();

    if (!keyword) {
        loadDocuments();
        return;
    }

    if (searchMode === 'title') {
        await searchByTitle(keyword);
    } else {
        await searchByContentKeywords(keyword);
    }
}

async function searchByTitle(keyword) {
    try {
        const response = await fetch(
            `${BASE_URL}/api/document/search?userId=${getUserId()}&keyword=${encodeURIComponent(keyword)}`,
            { headers: authHeaders() }
        );

        const data = await response.json();

        if (response.ok) {
            renderDocuments(data);
        } else {
            alert('Search failed');
        }

    } catch (error) {
        alert('Cannot connect to server');
    }
}
async function searchByContentKeywords(keywordInput) {
    const keywords = keywordInput.split(',').map(k => k.trim()).filter(k => k.length > 0);

    if (keywords.length === 0) {
        loadDocuments();
        return;
    }

    try {
        const queryParams = keywords.map(k => `keywords=${encodeURIComponent(k)}`).join('&');

const response = await fetch(
    `${BASE_URL}/api/search-index/search?${queryParams}&userId=${getUserId()}`,
            { headers: authHeaders() }
        );

        const data = await response.json();

        if (response.ok) {
            const documentIds = data.data.documentIds;

            if (documentIds.length === 0) {
                document.getElementById('documentsContainer').innerHTML =
                    '<div class="text-center text-muted py-4">No documents found for these keywords.</div>';
                return;
            }

            // Fetch full document details for each found ID
            await loadDocumentsByIds(documentIds);

        } else {
            alert('Search failed');
        }

    } catch (error) {
        alert('Cannot connect to server');
    }
}

async function loadDocumentsByIds(documentIds) {
    try {
        // Get all user documents first
        const response = await fetch(
            `${BASE_URL}/api/document/user/${getUserId()}`,
            { headers: authHeaders() }
        );

        const allDocuments = await response.json();

        if (response.ok) {
            // Filter only documents matching the found IDs
            const matchedDocuments = allDocuments.filter(doc =>
                documentIds.includes(doc.id)
            );

            if (matchedDocuments.length === 0) {
                document.getElementById('documentsContainer').innerHTML =
                    '<div class="text-center text-muted py-4">Documents found in index but not accessible. Try re-uploading.</div>';
                return;
            }

            renderDocuments(matchedDocuments);
        }

    } catch (error) {
        alert('Cannot connect to server');
    }
}


function clearSearch() {
    document.getElementById('searchInput').value = '';
    loadDocuments();
}


async function loadTopKeywords() {
    try {
        const response = await fetch(
            `${BASE_URL}/api/search-index/top-keywords?limit=15&userId=${getUserId()}`,
            { headers: authHeaders() }
        );

        const data = await response.json();

        if (response.ok) {
            const keywords = data.data.topKeywords;
            const container = document.getElementById('topKeywordsContainer');

            if (keywords.length === 0) {
                container.innerHTML = '<small class="text-muted">No keywords yet. Upload some documents first.</small>';
                return;
            }

            container.innerHTML = keywords
                .map(k => `<span class="topic-badge" onclick="searchByKeywordClick('${k}')" 
                            style="cursor:pointer">${k}</span>`)
                .join('');
        }

    } catch (error) {
        console.error('Failed to load top keywords', error);
    }
}

function searchByKeywordClick(keyword) {
    setSearchMode('content');
    document.getElementById('searchInput').value = keyword;
    searchDocuments();

}
