import React from 'react';

export default function Logo({ size = 'sm' }) {
  const isLarge = size === 'lg';
  return (
    <div className="logo">
      <div className="logo__mark" style={{ width: isLarge ? '48px' : '36px', height: isLarge ? '48px' : '36px' }}>
        <svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg" style={{ width: '100%', height: '100%' }}>
          <polygon points="50,5 95,30 95,80 50,95 5,80 5,30" stroke="var(--teal-glow)" strokeWidth="8" fill="rgba(0, 240, 200, 0.05)" />
          <polygon points="50,20 80,38 80,72 50,82 20,72 20,38" stroke="var(--teal-mid)" strokeWidth="6" fill="rgba(0, 240, 200, 0.1)" />
          <circle cx="50" cy="50" r="10" fill="var(--teal-glow)" />
        </svg>
      </div>
      <span className="logo__name" style={{ fontSize: isLarge ? '1.8rem' : '1.3rem' }}>
        NEXUS<span>AUTH</span>
      </span>
    </div>
  );
}
