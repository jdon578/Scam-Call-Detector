// Import Firebase functions
// Import the functions you need from the SDKs you need
  import { initializeApp } from "https://www.gstatic.com/firebasejs/13.0.0/firebase-app.js";
  import { getAnalytics } from "https://www.gstatic.com/firebasejs/13.0.0/firebase-analytics.js";
  import { getFirestore } from "https://www.gstatic.com/firebasejs/13.0.0/firebase-firestore.js";
  import { collection } from "https://www.gstatic.com/firebasejs/13.0.0/firebase-firestore.js";
  import { getDocs } from "https://www.gstatic.com/firebasejs/13.0.0/firebase-firestore.js";
  import { doc } from "https://www.gstatic.com/firebasejs/13.0.0/firebase-firestore.js";
  import { setDoc } from "https://www.gstatic.com/firebasejs/13.0.0/firebase-firestore.js";
  import { updateDoc } from "https://www.gstatic.com/firebasejs/13.0.0/firebase-firestore.js";

// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
    apiKey: "AIzaSyB4FxYWRmvk8zOLDPGirVSFuKZaLMpHqYc",
    authDomain: "scam-call-detector-bad53.firebaseapp.com",
    projectId: "scam-call-detector-bad53",
    storageBucket: "scam-call-detector-bad53.firebasestorage.app",
    messagingSenderId: "726500644153",
    appId: "1:726500644153:web:532b95c21a50cbd769c4e8",
    measurementId: "G-S8WJPK70SN"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const db = getFirestore(app);

async function pendingReportedNumbers() {
    const container = document.querySelector('#pending_numbers');
    try {
        container.innerHTML = "";
        const reports = await getDocs(collection(db, "phone_numbers"));
        reports.forEach((doc) => {
            const report = doc.data();
            if(report.status == "pending"){
                const card = document.createElement('div');
                card.classList.add('card');
                const phone_numbers = document.createElement('p');
                phone_numbers.textContent = "Phone Number: " + report.phoneNumber;

                const location = document.createElement('p');
                location.textContent = "Location: " + report.location;

                const scam_type = document.createElement('p');
                scam_type.textContent = "Scam Type: " + report.scamType;

                const name_attached = document.createElement('p');
                name_attached.textContent = "Name Attached: " + report.nameAttached;

                const other = document.createElement('p');
                other.textContent = "Additional Information: " + report.other;

                const status = document.createElement('p');
                status.textContent = "status: pending";
                status.className = "status";

                const button = document.createElement('button');
                button.textContent = "Verify";
                button.addEventListener('click', async () => {
                      await verifyReportedNumbers(doc.id);
                      console.log("Verify clicked:", doc.id);
                });


                card.append(phone_numbers,name_attached, scam_type, location, other, status, button);
                container.appendChild(card);

            }
            else if(report.status === "verified"){
                const card = document.createElement('div');
                card.classList.add('card');
                const verifiedStatus = document.createElement('p');
                verifiedStatus.textContent = "status: verified";
                verifiedStatus.className = "verifiedStatus";
                card.append(verifiedStatus);
                container.appendChild(card);

            }
          });

       }
       catch(error){
           console.log("error");
       }

    }

async function verifyReportedNumbers(id) {
    try {
        const reportRef = doc(db, "phone_numbers", id);
        const reports = await getDocs(collection(db, "phone_numbers"));

       let data = null;
       reports.forEach((doc) => {
            if(doc.id == id){
                data = doc.data();
            }
       });

       const verifiedRef = doc(db, "verified_numbers", id);

       await setDoc(verifiedRef, {
             ...data,
             status: "verified"
       });

      await updateDoc(reportRef, {
      status: "verified"
      });
      pendingReportedNumbers();

       }
    catch(error){
        console.log("error");}

}

    pendingReportedNumbers();


