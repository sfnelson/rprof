document.body.onclick = function (e) {
	var t = e.target;
	while (t.className != 'class' && t.nodeName != "SECTION") {
		t = t.parentNode;
		if (t == null) return;
	}
	for (var i in t.childNodes) {
		var c = t.childNodes[i];
		var cn = c.className;
		if (c.className && cn.match('content')) {
			if (cn.match('show')) {
				c.className = cn.replace('show','').trim();
			} else {
				c.className = cn + " show";
			}
		}
	}
}