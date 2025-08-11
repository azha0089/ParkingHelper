// 写法2：直接导出函数（ES6导出声明）
export const getRole = () => {
    return sessionStorage.getItem("role");
};

/**
 * 动态加载外部脚本
 * @param {string} src 脚本地址
 * @returns {Promise} 加载完成的Promise
 */
export function loadScript(src) {
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.src = src
    script.async = true
    script.onerror = reject
    script.onload = resolve
    document.head.appendChild(script)
  })
}
