/**
 * 将 js/cctv/tv.yml 转换为 js/cctv/tv3.yml
 * 规则：
 * 1) 把任何包含 ".../tv-web/.../live.html?url=" 的 URL，前缀都去掉为 "live.html?url="
 * 2) 排除（删除）所有以 "https://www.gdtv.cn" 开头的 URL 项
 */
const fs = require('fs');
const path = require('path');
const yaml = require('js-yaml');

const srcPath = path.join(__dirname, 'js', 'cctv', 'tv.yml');
const outPath = path.join(__dirname, 'js', 'cctv', 'tv2.yml');

function transformUrl(url) {
  // 规则2：排除 https://www.gdtv.cn 前缀
  if (/^https:\/\/www\.huya\.com/i.test(url)) {
    return null; // 表示该项需要被移除
  }

  // 新增规则：排除 https://www.fengshows.com 前缀
 /* if (/^https:\/\/www\.fengshows\.com/i.test(url)) {
    return null; // 表示该项需要被移除
  }*/

  // 新规则：对于包含 live.html?url= 的链接，直接提取 url= 后面的值
 /* if (url.includes('live.html?url=')&&url.includes(".m3u8")) {
    const match = url.match(/live\.html\?url=(.+)$/i);
    if (match && match[1]) {
      return decodeURIComponent(match[1]);
    }
  }*/

  // 原规则1（已注释，保留以备还原）：移除任何 URL 中的 "tv-web/" 前缀，并保留其后的路径（如 js/tv/iapp/... 或 live.html?url=...）
  const lower = url.toLowerCase();
  const idx = lower.indexOf('tv-web/');
  if (idx !== -1) {
    url = url.substring(idx + 'tv-web/'.length);
  }

  return url;
}

function main() {
  const raw = fs.readFileSync(srcPath, 'utf8');
  const doc = yaml.load(raw);

  let removedCount = 0;
  let transformedCount = 0;

  if (doc && Array.isArray(doc.data)) {
    const newData = [];
    let emptyChannelCount = 0;
    
    for (const group of doc.data) {
      if (Array.isArray(group.vods)) {
        const newVods = [];
        for (const vod of group.vods) {
          if (!vod || typeof vod.url !== 'string') {
            newVods.push(vod);
            continue;
          }
          const newUrl = transformUrl(vod.url);
          if (newUrl === null) {
            removedCount++;
            continue; // 删除该 vod
          }
          if (newUrl !== vod.url) {
            transformedCount++;
          }
          newVods.push({ ...vod, url: newUrl });
        }
        
        // 如果过滤后该频道还有内容，则保留；否则删除整个频道
        if (newVods.length > 0) {
          group.vods = newVods;
          newData.push(group);
        } else {
          emptyChannelCount++;
          console.log(`删除空频道: ${group.name || group.tag}`);
        }
      } else {
        newData.push(group);
      }
    }
    
    doc.data = newData;
    console.log(`共删除 ${emptyChannelCount} 个空频道`);
  }

  const outYaml = yaml.dump(doc, { lineWidth: -1 });
  fs.writeFileSync(outPath, outYaml, 'utf8');

  console.log(`转换完成：${outPath}`);
  console.log(`已过滤掉 gdtv.cn URL 数量：${removedCount}`);
  console.log(`已规范化 tv-web 前缀 URL 数量：${transformedCount}`);
}

main();
