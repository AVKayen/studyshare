function convertUTCDateToLocalDate(epoch) {
    let date = new Date(0);
    date.setUTCSeconds(epoch);
    return date.toLocaleString();
}

function appendValue(input, newValue) {
    input.value += newValue;
    input.value += ";";
}

function incContent(tag) {
    const prevValue = parseInt(tag.textContent)
    tag.textContent = prevValue + 1
}