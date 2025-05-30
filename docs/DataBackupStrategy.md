# Data Backup Strategy for AnkiZero

Ensuring user data is not lost is crucial. This document outlines potential strategies for data backup and recovery for the AnkiZero application.

## 1. Android Auto Backup (Default)

### Description:
Android's Auto Backup feature automatically backs up app data (including files in app-specific directories, shared preferences, and databases like Room) to the user's Google Drive. This is available for apps targeting Android 6.0 (API level 23) or higher.

### How it applies to AnkiZero:
- **Room Database:** By default, Room database files are included in Auto Backup if the app meets the conditions (e.g., not opted out, under quota).
- **Shared Preferences:** Notification preferences or other settings stored in SharedPreferences would also be backed up.

### Considerations:
- **Quota:** There's a 25MB per-user limit. If AnkiZero's database (especially with embedded images/audio in the future) grows larger, Auto Backup might become incomplete or fail for that data.
- **Configuration:** Developers can customize Auto Backup rules using XML to include/exclude specific files or directories. For AnkiZero, ensuring the Room database is explicitly included (or not excluded) would be important.
- **Restoration:** Data is typically restored when the app is reinstalled, such as on a new device.
- **Security:** Data is stored in the user's private Google Drive folder and is protected by their Google Account credentials.

### Action Items:
- Verify default Auto Backup behavior with the Room database.
- Define an XML backup ruleset if customization is needed (e.g., to exclude temporary cache files but ensure the database is included).
- Inform users about this default backup mechanism.

## 2. Manual Export/Import

### Description:
Allow users to manually export their flashcard data to a file, which they can then store locally or in their preferred cloud storage service. They can also import this data back into the app.

### How it applies to AnkiZero:
- **Export Format:**
    - **CSV/TSV:** Simple, human-readable, good for text-based content. Could include columns for French, English, pronunciation, example, notes, and SRS parameters (interval, ease factor, due date).
    - **JSON:** More structured, can better handle complex data or hierarchical structures if needed in the future.
    - **Custom Format (e.g., `.apkg` like Anki Desktop):** More complex to implement but could include media files and preserve rich formatting if those features are added.
- **Export Destination:** User's local storage (e.g., Downloads folder via Storage Access Framework) or allow sharing via system share sheet to cloud services.
- **Import Functionality:** The app would need to parse the chosen format and create/update flashcards accordingly. Conflict resolution (e.g., for cards with same ID or content) would need consideration.

### Action Items:
- Design the export format(s).
- Implement export functionality (accessible from Card Management screen).
- Implement import functionality.
- Provide clear instructions to users on how to use this feature.

## 3. Cloud Sync (Advanced - Future Consideration)

### Description:
Implement a dedicated cloud synchronization solution where user data is automatically synced to a cloud backend.

### How it applies to AnkiZero:
- **Backend Service:**
    - **Firebase Firestore/Realtime Database:** Good options for structured data, real-time sync, and user authentication.
    - **Custom Backend:** More control but also more development effort.
- **User Authentication:** Users would need to sign in (e.g., Google Sign-In, email/password) to link their data to an account.
- **Sync Logic:** Implement robust sync logic to handle online/offline scenarios, conflict resolution (if data can be modified on multiple devices simultaneously), and data merging.
- **Data Privacy & Security:** Ensure compliance with data privacy regulations and secure data transmission/storage.

### Considerations:
- **Complexity:** This is the most complex option to implement and maintain.
- **Cost:** Cloud services usually have associated costs based on usage (storage, data transfer, operations).
- **User Trust:** Users need to trust the app with their data in the cloud.

### Action Items (Long-term):
- Evaluate backend service options.
- Design data models for cloud storage.
- Implement user authentication.
- Develop and test sync logic thoroughly.

## Initial Recommendation:

1.  **Phase 1:** Rely on **Android Auto Backup** and clearly document its behavior and limitations to the user. Implement **Manual Export/Import** as the primary user-controlled backup method, starting with a simple format like CSV or JSON.
2.  **Phase 2 (Future):** Consider implementing **Cloud Sync** if there's strong user demand and resources allow, as it provides the most seamless and robust backup/sync experience across multiple devices.

This multi-layered approach provides a good balance of default protection, user control, and potential for future enhancements.
