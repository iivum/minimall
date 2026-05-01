/* ============================================
   Minimall Theme Switcher
   Version: 1.0.0
   Dark Mode & Theme Management
   ============================================ */

(function() {
  'use strict';

  const THEME_KEY = 'minimall-theme';
  const THEMES = ['light', 'dark'];

  // ============================================
  // Theme Manager
  // ============================================
  const ThemeManager = {
    getPreferredTheme: function() {
      const stored = localStorage.getItem(THEME_KEY);
      if (stored && THEMES.includes(stored)) {
        return stored;
      }
      return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    },

    setTheme: function(theme) {
      if (!THEMES.includes(theme)) {
        console.warn('[ThemeManager] Invalid theme:', theme);
        return;
      }
      localStorage.setItem(THEME_KEY, theme);
      document.documentElement.setAttribute('data-theme', theme);
      this.updateToggleButton(theme);
      this.dispatchThemeEvent(theme);
    },

    toggleTheme: function() {
      const current = this.getPreferredTheme();
      const next = current === 'light' ? 'dark' : 'light';
      this.setTheme(next);
      return next;
    },

    updateToggleButton: function(theme) {
      const btn = document.querySelector('[data-theme-toggle]');
      if (btn) {
        btn.setAttribute('aria-label', theme === 'light' ? 'Switch to dark mode' : 'Switch to light mode');
        btn.textContent = theme === 'light' ? '🌙' : '☀️';
      }
    },

    dispatchThemeEvent: function(theme) {
      window.dispatchEvent(new CustomEvent('themechange', { detail: { theme: theme } }));
    }
  };

  // ============================================
  // Initialize Theme
  // ============================================
  function initTheme() {
    const theme = ThemeManager.getPreferredTheme();
    document.documentElement.setAttribute('data-theme', theme);
    ThemeManager.updateToggleButton(theme);

    // Listen for system preference changes
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', function(e) {
      if (!localStorage.getItem(THEME_KEY)) {
        ThemeManager.setTheme(e.matches ? 'dark' : 'light');
      }
    });
  }

  // ============================================
  // Theme Toggle Handler
  // ============================================
  function handleThemeToggle(e) {
    if (e) {
      e.preventDefault();
    }
    ThemeManager.toggleTheme();
  }

  // ============================================
  // Auto-initialize on DOM Ready
  // ============================================
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initTheme);
  } else {
    initTheme();
  }

  // ============================================
  // Expose API for manual control
  // ============================================
  window.MinimallTheme = {
    toggle: handleThemeToggle,
    set: ThemeManager.setTheme.bind(ThemeManager),
    get: ThemeManager.getPreferredTheme.bind(ThemeManager)
  };

  // ============================================
  // Event Delegation for Toggle Button
  // ============================================
  document.addEventListener('click', function(e) {
    const toggleBtn = e.target.closest('[data-theme-toggle]');
    if (toggleBtn) {
      handleThemeToggle(e);
    }
  });

})();
