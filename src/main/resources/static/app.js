const form = document.getElementById('checkForm');
const urlInput = document.getElementById('urlInput');
const result = document.getElementById('result');
const statusEl = document.getElementById('status');
const detailsEl = document.getElementById('details');

async function checkUrl(url) {
  statusEl.textContent = 'Checking...';
  statusEl.className = '';
  detailsEl.textContent = '';
  result.classList.remove('hidden');

  try {
    const res = await fetch('/api/check-url', {
      method: 'POST',
      headers: {'Content-Type':'application/json'},
      body: JSON.stringify({url})
    });
    const json = await res.json();
    const msg = json && json.message ? json.message : JSON.stringify(json);
    statusEl.textContent = msg;
    if (msg.includes('✅')) {
      statusEl.classList.add('success');
    } else {
      statusEl.classList.add('fail');
    }
    detailsEl.textContent = JSON.stringify(json, null, 2);
  } catch (err) {
    statusEl.textContent = 'Request failed';
    statusEl.classList.add('fail');
    detailsEl.textContent = err.toString();
  }
}

form.addEventListener('submit', e => {
  e.preventDefault();
  const url = urlInput.value.trim();
  if (!url) return;
  checkUrl(url);
});

document.querySelectorAll('.example').forEach(b => b.addEventListener('click', () => { urlInput.value = b.textContent; checkUrl(b.textContent); }));
