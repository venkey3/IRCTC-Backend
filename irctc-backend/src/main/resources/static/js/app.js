document.addEventListener("DOMContentLoaded", () => {
  const list = document.querySelector("[data-passenger-list]");
  const addButton = document.querySelector("[data-add-passenger]");

  if (!list || !addButton) return;

  addButton.addEventListener("click", () => {
    const rows = list.querySelectorAll("[data-passenger-row]");
    const nextIndex = rows.length;
    const clone = rows[0].cloneNode(true);

    clone.querySelectorAll("input, select").forEach((field) => {
      field.name = field.name.replace(/passengers\[\d+\]/, `passengers[${nextIndex}]`);
      field.id = field.id.replace(/passengers\d+/, `passengers${nextIndex}`);
      if (field.tagName === "SELECT") field.selectedIndex = 0;
      else field.value = "";
    });

    clone.querySelectorAll("label").forEach((label) => {
      const target = label.getAttribute("for");
      if (target) label.setAttribute("for", target.replace(/passengers\d+/, `passengers${nextIndex}`));
    });

    clone.querySelector("legend").textContent = `Passenger ${nextIndex + 1}`;

    list.appendChild(clone);
  });

  list.addEventListener("click", (event) => {
    const removeButton = event.target.closest("[data-remove-passenger]");
    if (!removeButton) return;

    const rows = list.querySelectorAll("[data-passenger-row]");
    if (rows.length <= 1) return;
    removeButton.closest("[data-passenger-row]").remove();

    list.querySelectorAll("[data-passenger-row]").forEach((row, index) => {
      row.querySelector("legend").textContent = `Passenger ${index + 1}`;
      row.querySelectorAll("input, select").forEach((field) => {
        field.name = field.name.replace(/passengers\[\d+\]/, `passengers[${index}]`);
        field.id = field.id.replace(/passengers\d+/, `passengers${index}`);
      });
      row.querySelectorAll("label").forEach((label) => {
        const target = label.getAttribute("for");
        if (target) label.setAttribute("for", target.replace(/passengers\d+/, `passengers${index}`));
      });
    });
  });
});
