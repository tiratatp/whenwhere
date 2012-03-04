// If the element's string matches the regular expression it is numbers and letters
function isAlphanumeric(value) {
    if (value.match(/^[0-9a-zA-Z]+$/)) {
        return true;
    } else {
        return false;
    }
}

function showLoading() {
    $.mobile.showPageLoadingMsg();
}

function hideLoading() {
    $.mobile.hidePageLoadingMsg();
}

function dump(arr, level) {
    var dumped_text = "";
    if (!level) {
        level = 0
    }
    var level_padding = "";
    for (var j = 0; j < level + 1; j++) {
        level_padding += "    "
    }
    if (typeof arr == "object") {
        for (var item in arr) {
            var value = arr[item];
            if (typeof value == "object") {
                dumped_text += level_padding + "'" + item + "' ...\n";
                dumped_text += dump(value, level + 1)
            } else {
                dumped_text += level_padding + "'" + item + "' => \"" + value + '"\n'
            }
        }
    } else {
        dumped_text = "===>" + arr + "<===(" + typeof arr + ")"
    }
    return dumped_text
}

function getGravatar(email) {
    var url = 'http://www.gravatar.com/avatar/' + hex_md5(email) + '.jpg?s=80';
    return url;
}