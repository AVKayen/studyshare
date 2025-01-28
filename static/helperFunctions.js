function convertUTCDateToLocalDate(epoch) {
    let date = new Date(0);
    date.setUTCSeconds(epoch);
    return date.toLocaleString();
}