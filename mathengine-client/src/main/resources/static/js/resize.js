window.addEventListener("DOMContentLoaded", () => {
    const leftPane = document.querySelector(".left-pane");
    const vResizer = document.querySelector(".vertical-resizer");
    const topSection = document.querySelector(".top-section");
    const hResizer = document.querySelector(".horizontal-resizer");

    // === Vertical Resize (Left Pane) ===
    function handleVResize(startX) {
        const container = leftPane.parentNode;
        const containerRect = container.getBoundingClientRect();
        const minWidth = 150;
        const maxWidth = containerRect.width - 100;

        let newWidth = startX - containerRect.left;
        newWidth = Math.max(minWidth, Math.min(newWidth, maxWidth));

        leftPane.style.flex = "none";
        leftPane.style.width = newWidth + "px";

        // Store in localStorage
        localStorage.setItem("leftPaneWidth", newWidth);
    }

    function startVResize(e) {
        e.preventDefault();
        const moveHandler = (e) => {
            const clientX = e.touches ? e.touches[0].clientX : e.clientX;
            handleVResize(clientX);
        };

        const upHandler = () => {
            document.removeEventListener("mousemove", moveHandler);
            document.removeEventListener("mouseup", upHandler);
            document.removeEventListener("touchmove", moveHandler);
            document.removeEventListener("touchend", upHandler);
        };

        document.addEventListener("mousemove", moveHandler);
        document.addEventListener("mouseup", upHandler);
        document.addEventListener("touchmove", moveHandler);
        document.addEventListener("touchend", upHandler);
    }

    vResizer.addEventListener("mousedown", startVResize);
    vResizer.addEventListener("touchstart", startVResize, {passive: false});

    // === Horizontal Resize (Top Section) ===
    function handleHResize(startY) {
        const container = topSection.parentNode;
        const containerRect = container.getBoundingClientRect();
        const minHeight = 200;
        const maxHeight = containerRect.height - 100;

        let newHeight = startY - containerRect.top;
        newHeight = Math.max(minHeight, Math.min(newHeight, maxHeight));

        topSection.style.flex = "none";
        topSection.style.height = newHeight + "px";

        // Store in localStorage
        localStorage.setItem("topSectionHeight", newHeight);
    }

    function startHResize(e) {
        e.preventDefault();
        const moveHandler = (e) => {
            const clientY = e.touches ? e.touches[0].clientY : e.clientY;
            handleHResize(clientY);
        };

        const upHandler = () => {
            document.removeEventListener("mousemove", moveHandler);
            document.removeEventListener("mouseup", upHandler);
            document.removeEventListener("touchmove", moveHandler);
            document.removeEventListener("touchend", upHandler);
        };

        document.addEventListener("mousemove", moveHandler);
        document.addEventListener("mouseup", upHandler);
        document.addEventListener("touchmove", moveHandler);
        document.addEventListener("touchend", upHandler);
    }

    hResizer.addEventListener("mousedown", startHResize);
    hResizer.addEventListener("touchstart", startHResize, {passive: false});

    // === Restore previous sizes from localStorage ===
    const savedLeftWidth = localStorage.getItem("leftPaneWidth");
    if (savedLeftWidth) {
        leftPane.style.flex = "none";
        leftPane.style.width = savedLeftWidth + "px";
    }

    const savedTopHeight = localStorage.getItem("topSectionHeight");
    if (savedTopHeight) {
        topSection.style.flex = "none";
        topSection.style.height = savedTopHeight + "px";
    }
});

document.body.classList.add('loaded');
