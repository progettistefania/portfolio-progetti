// SLIDESHOW
const slides = document.querySelectorAll('.slide');
let currentIndex = 0;

function showSlide(index) {
  slides.forEach((slide, i) => {
    slide.classList.toggle('active', i === index);
  });
}

function nextSlide() {
  currentIndex = (currentIndex + 1) % slides.length;
  showSlide(currentIndex);
}

function prevSlide() {
  currentIndex = (currentIndex - 1 + slides.length) % slides.length;
  showSlide(currentIndex);
}

// DOM READY
document.addEventListener('DOMContentLoaded', () => {
  // Init slideshow
  showSlide(currentIndex);
  document.getElementById('prev').addEventListener('click', prevSlide);
  document.getElementById('next').addEventListener('click', nextSlide);
  setInterval(nextSlide, 3000);

  // Elementi video e pulsante mute
  const video = document.getElementById("fullscreenVideo");
  const muteBtn = document.getElementById("muteButton");
  const muteIcon = muteBtn ? muteBtn.querySelector("i") : null;

  // Elementi demo audio
  const playDemoBtn = document.getElementById('playDemoBtn');
  const demoAudio = document.getElementById('demoAudio');

  // Gestione mute video
  if (video && muteBtn && muteIcon) {
    muteBtn.addEventListener("click", () => {
      video.muted = !video.muted;
      if (video.muted) {
        muteIcon.classList.remove("fa-volume-up");
        muteIcon.classList.add("fa-volume-mute");
      } else {
        muteIcon.classList.remove("fa-volume-mute");
        muteIcon.classList.add("fa-volume-up");

        // Se si riattiva l'audio video mentre la demo è attiva, fermala
        if (!demoAudio.paused) {
          demoAudio.pause();
          playDemoBtn.textContent = '🎵 Ascolta uno dei brani';
        }
      }
    });
  }

  // Gestione demo audio
  if (playDemoBtn && demoAudio && video && muteIcon) {
    playDemoBtn.addEventListener('click', () => {
      if (demoAudio.paused) {
        // Avvia demo audio
        demoAudio.currentTime = 0;
        demoAudio.play();
        playDemoBtn.textContent = '⏸️ Pausa';

        // Ferma e muta il video
        if (!video.paused) {
          video.pause();
        }
        video.muted = true;
        muteIcon.classList.remove("fa-volume-up");
        muteIcon.classList.add("fa-volume-mute");
      } else {
        // Ferma demo audio
        demoAudio.pause();
        playDemoBtn.textContent = '🎵 Ascolta uno dei brani';

        // Riavvia video mutato
        video.muted = true;
        video.play();
        muteIcon.classList.remove("fa-volume-up");
        muteIcon.classList.add("fa-volume-mute");
      }
    });

    // Se audio demo viene stoppato (fine brano o interruzione), riavvia video mutato
    demoAudio.addEventListener('pause', () => {
      video.muted = true;
      video.play();
      muteIcon.classList.remove("fa-volume-up");
      muteIcon.classList.add("fa-volume-mute");
      playDemoBtn.textContent = '🎵 Ascolta uno dei brani';
    });
  }

  // === GESTIONE ADATTIVA SIDEBAR ===
  const isTouchDevice = 'ontouchstart' in window || navigator.maxTouchPoints > 0;
  const sidebar = document.querySelector('.sidebar');
  const sidebarLinks = document.querySelectorAll('.sidebar a');
  const homeLink = document.querySelector('.navbar a[href="#home"]');

  if (sidebar) {
    // Mobile: aggiunge "expanded" solo se touch
    sidebarLinks.forEach(link => {
      link.addEventListener('click', () => {
        if (isTouchDevice) {
          sidebar.classList.add('expanded');
        }
      });
    });

    // Torna al comportamento hover su home
    if (homeLink) {
      homeLink.addEventListener('click', () => {
        sidebar.classList.remove('expanded');
      });
    }
  }

  // === GESTIONE STOP AUDIO/VIDEO SU CLICK LINK ESTERNI ===
  //const externalLinks = document.querySelectorAll('a.external-link');
  
  const externalLinks = document.querySelectorAll(
  'a[href*="youtube.com"], a[href*="youtu.be"], a[href*="instagram.com"], a[href*="facebook.com"], a[href*="spotify.com"]'
);

  externalLinks.forEach(link => {
    link.addEventListener('click', () => {
      // Ferma audio demo se attivo
      if (demoAudio && !demoAudio.paused) {
        demoAudio.pause();
        playDemoBtn.textContent = '🎵 Ascolta uno dei brani';
      }

      // Ferma e muta il video
      if (video && !video.paused) {
        video.pause();
      }
      if (video && !video.muted) {
        video.muted = true;
        if (muteIcon) {
          muteIcon.classList.remove("fa-volume-up");
          muteIcon.classList.add("fa-volume-mute");
        }
      }
    });
  });

  // Quando torni alla tab visibile (dopo aver chiuso YouTube)
  document.addEventListener('visibilitychange', () => {
    if (!document.hidden) {
      // Se demoAudio è fermo, riavvia video mutato
      if (demoAudio && demoAudio.paused && video) {
        video.muted = true;
        video.play();
        if (muteIcon) {
          muteIcon.classList.remove("fa-volume-up");
          muteIcon.classList.add("fa-volume-mute");
        }
      }
    }
  });

}); // Fine DOMContentLoaded
