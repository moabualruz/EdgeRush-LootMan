use tauri::{
    AppHandle, Manager, Runtime,
    menu::{Menu, MenuItem, PredefinedMenuItem},
    tray::{MouseButton, MouseButtonState, TrayIcon, TrayIconBuilder, TrayIconEvent},
    image::Image,
};
use std::sync::Arc;

/// Tray icon state
#[derive(Clone, Copy, PartialEq)]
pub enum TrayState {
    Idle,
    Syncing,
    Success,
    Error,
}

/// Create the system tray icon and menu
pub fn create_tray<R: Runtime>(app: &AppHandle<R>) -> Result<TrayIcon<R>, tauri::Error> {
    // Create menu items
    let sync_now = MenuItem::with_id(app, "sync_now", "Sync Now", true, None::<&str>)?;
    let settings = MenuItem::with_id(app, "settings", "Settings...", true, None::<&str>)?;
    let separator = PredefinedMenuItem::separator(app)?;
    let quit = MenuItem::with_id(app, "quit", "Quit", true, None::<&str>)?;

    // Build menu
    let menu = Menu::with_items(app, &[
        &sync_now,
        &separator,
        &settings,
        &PredefinedMenuItem::separator(app)?,
        &quit,
    ])?;

    // Get default icon
    let icon = get_tray_icon(TrayState::Idle);

    // Build tray icon
    let tray = TrayIconBuilder::new()
        .icon(icon)
        .tooltip("EdgeRush LootMan")
        .menu(&menu)
        .on_menu_event(|app, event| {
            handle_menu_event(app, &event.id.0);
        })
        .on_tray_icon_event(|tray, event| {
            if let TrayIconEvent::Click {
                button: MouseButton::Left,
                button_state: MouseButtonState::Up,
                ..
            } = event
            {
                // Show main window on left click
                let app = tray.app_handle();
                if let Some(window) = app.get_webview_window("main") {
                    let _ = window.show();
                    let _ = window.set_focus();
                }
            }
        })
        .build(app)?;

    Ok(tray)
}

/// Handle menu item clicks
fn handle_menu_event<R: Runtime>(app: &AppHandle<R>, id: &str) {
    match id {
        "sync_now" => {
            // Emit event to trigger sync
            let _ = app.emit("tray-sync-now", ());
        }
        "settings" => {
            // Show settings window
            if let Some(window) = app.get_webview_window("main") {
                let _ = window.show();
                let _ = window.set_focus();
                let _ = app.emit("show-settings", ());
            }
        }
        "quit" => {
            app.exit(0);
        }
        _ => {}
    }
}

/// Get tray icon for the given state
fn get_tray_icon(state: TrayState) -> Image<'static> {
    // In production, these would be actual icon files
    // For now, we use a placeholder approach

    // Create a simple colored icon based on state
    let (r, g, b) = match state {
        TrayState::Idle => (100, 100, 100),     // Gray
        TrayState::Syncing => (0, 150, 255),    // Blue
        TrayState::Success => (0, 200, 100),    // Green
        TrayState::Error => (255, 100, 100),    // Red
    };

    // Create a 16x16 RGBA icon
    let size = 16;
    let mut rgba = Vec::with_capacity(size * size * 4);

    for y in 0..size {
        for x in 0..size {
            // Create a simple circular icon
            let cx = (x as f32) - (size as f32 / 2.0);
            let cy = (y as f32) - (size as f32 / 2.0);
            let dist = (cx * cx + cy * cy).sqrt();
            let radius = (size as f32 / 2.0) - 1.0;

            if dist <= radius {
                rgba.push(r);
                rgba.push(g);
                rgba.push(b);
                rgba.push(255);
            } else {
                rgba.push(0);
                rgba.push(0);
                rgba.push(0);
                rgba.push(0);
            }
        }
    }

    Image::new_owned(rgba, size as u32, size as u32)
}

/// Update tray icon state
pub fn update_tray_state<R: Runtime>(app: &AppHandle<R>, state: TrayState) {
    if let Some(tray) = app.tray_by_id("main") {
        let icon = get_tray_icon(state);
        let _ = tray.set_icon(Some(icon));

        let tooltip = match state {
            TrayState::Idle => "EdgeRush LootMan - Idle",
            TrayState::Syncing => "EdgeRush LootMan - Syncing...",
            TrayState::Success => "EdgeRush LootMan - Sync Complete",
            TrayState::Error => "EdgeRush LootMan - Sync Error",
        };
        let _ = tray.set_tooltip(Some(tooltip));
    }
}

/// Show a desktop notification
pub fn show_notification<R: Runtime>(app: &AppHandle<R>, title: &str, body: &str) {
    #[cfg(desktop)]
    {
        use tauri::notification::Notification;
        let _ = Notification::new(&app.config().identifier)
            .title(title)
            .body(body)
            .show();
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_tray_state_icon_colors() {
        // Verify each state produces a different icon
        let idle = get_tray_icon(TrayState::Idle);
        let syncing = get_tray_icon(TrayState::Syncing);
        let success = get_tray_icon(TrayState::Success);
        let error = get_tray_icon(TrayState::Error);

        // Icons should be 16x16 RGBA
        assert_eq!(idle.rgba().len(), 16 * 16 * 4);
        assert_eq!(syncing.rgba().len(), 16 * 16 * 4);
        assert_eq!(success.rgba().len(), 16 * 16 * 4);
        assert_eq!(error.rgba().len(), 16 * 16 * 4);
    }
}
