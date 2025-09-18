document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.custom-textarea3').forEach(textarea => {
        textarea.addEventListener('click', () => {
            const calcId = textarea.getAttribute('data-id');
            document.getElementById('cald-id-field').value = calcId;
            document.getElementById('calculation-selection').submit();
        });
    });
});
