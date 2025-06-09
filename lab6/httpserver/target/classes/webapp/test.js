// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 显示会话信息
    displaySessionInfo();
    
    console.log('Java HTTP服务器客户端已加载');
    console.log('会话信息:', window.sessionInfo);
});

// 显示会话信息
function displaySessionInfo() {
    if (window.sessionInfo) {
        document.getElementById('session-id').innerHTML = 
            `访客ID: ${window.sessionInfo.visitorId}<br>` +
            `会话ID: ${window.sessionInfo.sessionId}<br>` +
            `首次访问: ${window.sessionInfo.firstVisit}<br>` +
            `最后访问: ${window.sessionInfo.lastVisit}<br>` +
            `访问次数: ${window.sessionInfo.visitCount}<br>` +
            `总访客数: ${window.sessionInfo.totalVisitors}`;
    } else {
        // 从cookie中读取会话ID（fallback）
        const cookies = document.cookie.split(';');
        let sessionInfo = '从Cookie读取:<br>';
        
        for (let cookie of cookies) {
            const [name, value] = cookie.trim().split('=');
            if (['SESSIONID', 'VISITOR_ID', 'FIRST_VISIT', 'VISIT_COUNT'].includes(name)) {
                sessionInfo += `${name}: ${value}<br>`;
            }
        }
        
        document.getElementById('session-id').innerHTML = sessionInfo || '未设置';
    }
}

// 测试GET请求
async function testGet() {
    const resultDiv = document.getElementById('get-result');
    
    try {
        const response = await fetch('/api/test', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        const data = await response.text();
        showResult(resultDiv, `GET请求成功! 状态: ${response.status}<br>响应: ${data}`, 'success');
        
    } catch (error) {
        showResult(resultDiv, `GET请求失败: ${error.message}`, 'error');
    }
}

// 测试HEAD请求
async function testHead() {
    const resultDiv = document.getElementById('head-result');
    
    try {
        const response = await fetch('/api/test', {
            method: 'HEAD'
        });
        
        showResult(resultDiv, `HEAD请求成功! 状态: ${response.status}<br>Content-Type: ${response.headers.get('content-type')}`, 'success');
        
    } catch (error) {
        showResult(resultDiv, `HEAD请求失败: ${error.message}`, 'error');
    }
}

// 测试POST请求
async function testPost(event) {
    event.preventDefault();
    
    const form = document.getElementById('post-form');
    const formData = new FormData(form);
    const resultDiv = document.getElementById('post-result');
    
    const params = new URLSearchParams();
    for (const [key, value] of formData) {
        params.append(key, value);
    }
    
    try {
        const response = await fetch('/', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: params.toString()
        });
        
        const data = await response.text();
        showResult(resultDiv, `POST请求成功! 检查响应页面查看详细会话信息`, 'success');
        
        // 在新窗口显示POST响应
        const newWindow = window.open('', '_blank');
        newWindow.document.write(data);
        
    } catch (error) {
        showResult(resultDiv, `POST请求失败: ${error.message}`, 'error');
    }
    
    return false;
}

// 测试不同文件类型
async function testFileTypes() {
    const resultDiv = document.getElementById('file-test-result');
    const fileTypes = [
        { path: '/test.css', type: 'CSS样式文件' },
        { path: '/test.js', type: 'JavaScript文件' },
        { path: '/images/test.png', type: 'PNG图片文件' },
        { path: '/index.html', type: 'HTML页面' },
        { path: '/api/stats', type: 'API统计接口' }
    ];
    
    let results = [];
    
    for (const file of fileTypes) {
        try {
            const response = await fetch(file.path);
            const contentType = response.headers.get('content-type');
            results.push(`✓ ${file.type}: ${response.status} (${contentType})`);
        } catch (error) {
            results.push(`✗ ${file.type}: 失败 - ${error.message}`);
        }
    }
    
    showResult(resultDiv, `文件类型测试结果:<br>${results.join('<br>')}`, 'success');
}

// 显示结果
function showResult(element, message, type) {
    element.innerHTML = message;
    element.className = `result ${type}`;
    element.style.display = 'block';
    
    setTimeout(() => {
        element.style.display = 'none';
    }, 8000);
}

// 获取服务器统计信息
async function getServerStats() {
    try {
        const response = await fetch('/api/stats');
        const stats = await response.json();
        console.log('服务器统计:', stats);
        return stats;
    } catch (error) {
        console.error('获取服务器统计失败:', error);
    }
}

// 页面性能监控
window.addEventListener('load', function() {
    const loadTime = performance.now();
    console.log(`页面加载时间: ${loadTime.toFixed(2)}ms`);
});

// 用于增加文件大小
// 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
// 0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
