function bodyLoad() {
    nav_buttons();
};

function bodyResize() {
    //FIXME: "scroll backward" if we have room for more screenshots
    nav_buttons();
};

function nav_back() {
    var containers = document.getElementsByClassName("screenshots");
    if (containers.length > 0) {
        var container = containers[0];
        var screenshots = container.getElementsByTagName("img");
        
        if (screenshots[0].style.display == "none") {
            for (i = 1; i < screenshots.length; i++) {
                if (screenshots[i].style.display != "none") {
                    screenshots[i - 1].style.display = "inline";
                    break;
                }
            };
        };
        nav_buttons();
    };
};

function nav_forward() {
    var containers = document.getElementsByClassName("screenshots");
    if (containers.length > 0) {
        var container = containers[0];
        var screenshots = container.getElementsByTagName("img");
        
        var lastScreenshot = screenshots[screenshots.length - 1];
        
        // Screenshots with an offsetTop > 0 are those that did not fit on one row and got truncated
        if (lastScreenshot.offsetTop > 0) {
            for (i = 0; i < screenshots.length; i++) {
                if (screenshots[i].style.display != "none") {
                    screenshots[i].style.display = "none";
                    break;
                }
            };
        };
        nav_buttons();
    };
};

function nav_buttons() {
    var containers = document.getElementsByClassName("screenshots");
    if (containers.length > 0) {
        var container = containers[0];
        var backButton = document.getElementById("nav_back");
        var forwardButton = document.getElementById("nav_forward");
        var screenshots = container.getElementsByTagName("img");
        
        if (screenshots[0].style.display == "none") {
            backButton.style.display = "block";
        } else {
            backButton.style.display = "none";
        };
        
        if (screenshots[screenshots.length - 1].offsetTop > 0) {
            forwardButton.style.display = "block";
        } else {
            forwardButton.style.display = "none";
        };
        
    };
};
