function addMessage() {
    var message = {
        author: $("#author").val(),
        text: $("#text").val()
    };
    $.post(
        "/messages",
        JSON.stringify(message),
        function(data) {
            $("#text").val("");
        }
    );
}

function getMessages() {
    $.get(
        "/messages",
        function(data) {
            $("#messages").empty();
            var messages = JSON.parse(data).messages;
            for (var i in messages) {
                var author = messages[i].author;
                var text = messages[i].text;
                var elem = $("<div>");
                elem.text(author + ": " + text);
                $("#messages").append(elem);
            }
        }
    );
}

setInterval(getMessages, 1000);