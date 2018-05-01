function findSubs() {
    var subs = [];
    $(".d_title a").each(function (e) {
        var href = $(this).attr("href");
        if (/^\/.*$/.test(href)) {
            subs.push({ href: window.location.origin + href, text: $(this).text() });
        } else if (/^http.*$/.test(href)) {
            subs.push({ href: href, text: $(this).text() });
        } else {
            var idx = window.location.pathname.lastIndexOf("/");
            if (idx < 0) {
                subs.push({ href: window.location.origin + href, text: $(this).text() });
            } else {
                subs.push({ href: window.location.origin + window.location.pathname.substr(0, idx) + href, text: $(this).text() });
            }
        }
    });
    return JSON.stringify(subs);
}

function findSubs2() {
    var subs = [];
    var titles = document.getElementsByClassName("introtitle");
    for (var i = 0; i < titles.length; i++) {
        var title = titles[i];
        var href = title.href;
        var text = title.title;
        if (/^\/.*$/.test(href)) {
            subs.push({ href: window.location.origin + href, text: text });
        } else if (/^http.*$/.test(href)) {
            subs.push({ href: href, text: text });
        } else {
            var idx = window.location.pathname.lastIndexOf("/");
            if (idx < 0) {
                subs.push({ href: window.location.origin + href, text: text });
            } else {
                subs.push({ href: window.location.origin + window.location.pathname.substr(0, idx) + href, text: text });
            }
        }
    }
    return JSON.stringify(subs);
}

function clickDown() {
    document.getElementById("down").click();
}




function down() {
    var t = { sub_id: "", geetest_challenge: "", geetest_validate: "", geetest_seccode: "" };
    t.sub_id = $("#down").attr("sid"), t.geetest_challenge = $("input[name='geetest_challenge']").val(), t.geetest_validate = $("input[name='geetest_validate']").val(), t.geetest_seccode = $("input[name='geetest_seccode']").val();
    var e = $("#count").text();
    $.ajax({
        type: "POST", url: "/ajax/down_ajax", cache: !0, dataType: "json", data: t, success: function (t) {
            1 == t.success ? ($("#count").text(1 * e + 1), $("#down").attr("disabled", "disabled"), $("#down").text("下载如未开始可使用右边链接进行下载"), $("#down_url").html('<a href="' + t.url + '">下载链接</a>'), $("#down_url").addClass("red"), window.location.href = t.url) : alert(t.msg)
        }
    })
}