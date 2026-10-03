import { useEffect, useRef, useState } from 'react';

const QUERY = '(hover: hover) and (pointer: fine)';

export default function CustomCursor() {
  const dot = useRef(null);
  const ring = useRef(null);
  const [enabled, setEnabled] = useState(() => window.matchMedia(QUERY).matches);

  // Desktop <-> mobile view badalne par enabled update karo
  useEffect(() => {
    const mq = window.matchMedia(QUERY);
    const onChange = (e) => setEnabled(e.matches);
    mq.addEventListener('change', onChange);
    return () => mq.removeEventListener('change', onChange);
  }, []);

  // Cursor ka asli kaam, sirf enabled hone par
  useEffect(() => {
    if (!enabled) return;
    const dotEl = dot.current;
    const ringEl = ring.current;
    if (!dotEl || !ringEl) return;

    let mouseX = 0, mouseY = 0, ringX = 0, ringY = 0;
    let first = true;
    let raf;

    const onMove = (e) => {
      mouseX = e.clientX;
      mouseY = e.clientY;
      if (first) {
        ringX = mouseX;
        ringY = mouseY;
        first = false;
      }
      dotEl.classList.add('visible');
      ringEl.classList.add('visible');
      const t = e.target instanceof Element ? e.target : null;
      ringEl.classList.toggle('hover', Boolean(t && t.closest('a, button, select, input, textarea, label')));
    };

    const onLeave = () => {
      dotEl.classList.remove('visible');
      ringEl.classList.remove('visible');
    };

    const animate = () => {
      ringX += (mouseX - ringX) * 0.18;
      ringY += (mouseY - ringY) * 0.18;
      dotEl.style.transform = `translate(${mouseX}px, ${mouseY}px) translate(-50%, -50%)`;
      ringEl.style.transform = `translate(${ringX}px, ${ringY}px) translate(-50%, -50%)`;
      raf = requestAnimationFrame(animate);
    };
    raf = requestAnimationFrame(animate);

    window.addEventListener('mousemove', onMove);
    document.documentElement.addEventListener('mouseleave', onLeave);

    return () => {
      cancelAnimationFrame(raf);
      window.removeEventListener('mousemove', onMove);
      document.documentElement.removeEventListener('mouseleave', onLeave);
    };
  }, [enabled]);

  if (!enabled) return null;

  return (
    <>
      <div ref={ring} className="cursor-ring" aria-hidden="true" />
      <div ref={dot} className="cursor-dot" aria-hidden="true" />
    </>
  );
}