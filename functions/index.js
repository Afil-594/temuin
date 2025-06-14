const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.archiveOldReports = functions.pubsub.schedule("every 6 hours").onRun(async (context) => {
  console.log("Menjalankan fungsi arsip otomatis...");

  const now = Date.now();
  const twentyFourHoursAgo = now - 24 * 60 * 60 * 1000;

  const db = admin.database();
  const itemRefs = [db.ref("/lost_items"), db.ref("/found_items")];

  for (const ref of itemRefs) {
    const updates = {};

    // --- BAGIAN 1: MENANGANI LAPORAN DIVERIFIKASI ---
    const verifiedQuery = ref.orderByChild("status").equalTo("diverifikasi");
    const verifiedSnapshot = await verifiedQuery.once("value");

    if (verifiedSnapshot.exists()) {
      verifiedSnapshot.forEach(childSnapshot => {
        const item = childSnapshot.val();
        if (!item.adminArchived && item.verifiedTimestamp && item.verifiedTimestamp < twentyFourHoursAgo) {
          console.log(`Menandai item VERIFIKASI untuk diarsip: ${childSnapshot.key}`);
          updates[`${childSnapshot.key}/adminArchived`] = true;
        }
      });
    }

    // --- BAGIAN 2: MENANGANI LAPORAN DITOLAK ---
    const rejectedQuery = ref.orderByChild("status").equalTo("ditolak");
    const rejectedSnapshot = await rejectedQuery.once("value");

    if (rejectedSnapshot.exists()) {
      rejectedSnapshot.forEach(childSnapshot => {
        const item = childSnapshot.val();
        // Cek 'rejectedTimestamp'
        if (!item.adminArchived && item.rejectedTimestamp && item.rejectedTimestamp < twentyFourHoursAgo) {
          console.log(`Menandai item DITOLAK untuk diarsip: ${childSnapshot.key}`);
          updates[`${childSnapshot.key}/adminArchived`] = true;
        }
      });
    }

    // Lakukan update ke database jika ada item yang perlu diubah
    if (Object.keys(updates).length > 0) {
      console.log(`Mengarsipkan total ${Object.keys(updates).length} item di ${ref.path}...`);
      await ref.update(updates);
    }
  }
  
  console.log("Fungsi arsip otomatis selesai.");
  return null;
});