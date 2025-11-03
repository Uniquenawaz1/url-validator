
# Create index.html (Frontend)
html_content = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>URL Validator</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        .container {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            padding: 50px 40px;
            max-width: 600px;
            width: 100%;
        }

        h1 {
            color: #333;
            font-size: 2.5rem;
            margin-bottom: 10px;
            text-align: center;
        }

        .subtitle {
            color: #666;
            text-align: center;
            margin-bottom: 40px;
            font-size: 1.1rem;
        }

        .input-group {
            margin-bottom: 20px;
        }

        input[type="text"] {
            width: 100%;
            padding: 18px 20px;
            border: 2px solid #e0e0e0;
            border-radius: 12px;
            font-size: 1.1rem;
            transition: all 0.3s ease;
            outline: none;
        }

        input[type="text"]:focus {
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        button {
            width: 100%;
            padding: 18px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 12px;
            font-size: 1.2rem;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s, box-shadow 0.2s;
        }

        button:hover:not(:disabled) {
            transform: translateY(-2px);
            box-shadow: 0 10px 25px rgba(102, 126, 234, 0.4);
        }

        button:active:not(:disabled) {
            transform: translateY(0);
        }

        button:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }

        .result {
            margin-top: 30px;
            padding: 25px;
            border-radius: 12px;
            text-align: center;
            font-size: 1.1rem;
            animation: slideIn 0.3s ease;
        }

        @keyframes slideIn {
            from {
                opacity: 0;
                transform: translateY(-10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .result.valid {
            background: #d4edda;
            border: 2px solid #28a745;
            color: #155724;
        }

        .result.invalid {
            background: #f8d7da;
            border: 2px solid #dc3545;
            color: #721c24;
        }

        .result-icon {
            font-size: 3rem;
            margin-bottom: 10px;
        }

        .result-message {
            font-weight: 600;
            font-size: 1.3rem;
            margin-bottom: 10px;
        }

        .checked-url {
            color: #666;
            font-size: 0.95rem;
            word-break: break-all;
            margin-top: 10px;
        }

        .reset-btn {
            margin-top: 20px;
            background: #6c757d;
            padding: 12px;
            font-size: 1rem;
        }

        .reset-btn:hover {
            background: #5a6268;
        }

        .loading {
            display: inline-block;
            width: 20px;
            height: 20px;
            border: 3px solid rgba(255, 255, 255, 0.3);
            border-radius: 50%;
            border-top-color: white;
            animation: spin 1s linear infinite;
            margin-left: 10px;
            vertical-align: middle;
        }

        @keyframes spin {
            to { transform: rotate(360deg); }
        }

        .hidden {
            display: none;
        }

        .status-text {
            color: #666;
            text-align: center;
            margin-top: 15px;
            font-size: 0.95rem;
        }

        @media (max-width: 600px) {
            .container {
                padding: 30px 20px;
            }

            h1 {
                font-size: 2rem;
            }

            .subtitle {
                font-size: 1rem;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>URL Validator</h1>
        <p class="subtitle">Check if a website URL is valid and reachable</p>

        <div class="input-group">
            <input 
                type="text" 
                id="urlInput" 
                placeholder="Enter URL (e.g., https://example.com)"
                autocomplete="off"
            >
        </div>

        <button id="checkBtn" onclick="checkUrl()">
            <span id="btnText">Check URL</span>
        </button>

        <div id="status" class="status-text hidden"></div>
        <div id="result" class="result hidden"></div>
    </div>

    <script>
        const API_ENDPOINT = 'http://localhost:8080/api/check-url';
        
        async function checkUrl() {
            const urlInput = document.getElementById('urlInput');
            const url = urlInput.value.trim();
            const checkBtn = document.getElementById('checkBtn');
            const btnText = document.getElementById('btnText');
            const resultDiv = document.getElementById('result');
            const statusDiv = document.getElementById('status');

            if (!url) {
                alert('Please enter a URL');
                return;
            }

            // Validate URL format
            if (!url.startsWith('http://') && !url.startsWith('https://')) {
                alert('URL must start with http:// or https://');
                return;
            }

            // Disable button and show loading
            checkBtn.disabled = true;
            btnText.innerHTML = 'Checking<span class="loading"></span>';
            resultDiv.classList.add('hidden');
            statusDiv.textContent = 'Validating URL...';
            statusDiv.classList.remove('hidden');

            try {
                const response = await fetch(API_ENDPOINT, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ url: url })
                });

                const data = await response.json();
                displayResult(data.message, url);

            } catch (error) {
                console.error('Error:', error);
                displayError();
            } finally {
                checkBtn.disabled = false;
                btnText.textContent = 'Check URL';
                statusDiv.classList.add('hidden');
            }
        }

        function displayResult(message, url) {
            const resultDiv = document.getElementById('result');
            const isValid = message.includes('✅');

            resultDiv.className = 'result ' + (isValid ? 'valid' : 'invalid');
            resultDiv.innerHTML = `
                <div class="result-icon">${isValid ? '✅' : '❌'}</div>
                <div class="result-message">${message}</div>
                <div class="checked-url">Checked: ${url}</div>
                <button class="reset-btn" onclick="reset()">Check Another URL</button>
            `;
            resultDiv.classList.remove('hidden');
        }

        function displayError() {
            const resultDiv = document.getElementById('result');
            resultDiv.className = 'result invalid';
            resultDiv.innerHTML = `
                <div class="result-icon">⚠️</div>
                <div class="result-message">Cannot connect to backend API</div>
                <div class="checked-url">
                    Please ensure your Spring Boot server is running at:<br>
                    <strong>http://localhost:8080</strong>
                </div>
                <button class="reset-btn" onclick="reset()">Try Again</button>
            `;
            resultDiv.classList.remove('hidden');
        }

        function reset() {
            document.getElementById('urlInput').value = '';
            document.getElementById('result').classList.add('hidden');
            document.getElementById('urlInput').focus();
        }

        // Allow Enter key to submit
        document.getElementById('urlInput').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                checkUrl();
            }
        });
    </script>
</body>
</html>
"""

with open(f"{base_path}/src/main/resources/static/index.html", "w") as f:
    f.write(html_content)

print("Frontend (index.html) created!")
