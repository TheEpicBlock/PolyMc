fetch("./assets/links.html").then(async (result) => {
  if (result) {
    for (const p of document.getElementsByClassName("links")) {
      p.innerHTML = await result.text();
    }
  } else {
    for (const p of document.getElementsByClassName("links")) {
        p.innerHTML = "error, couldn't load links"
    }
  }
})