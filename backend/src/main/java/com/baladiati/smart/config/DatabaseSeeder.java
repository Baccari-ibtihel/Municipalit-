package com.baladiati.smart.config;

import com.baladiati.smart.entity.*;
import com.baladiati.smart.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRequestRepository requestRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private MarriageContractRepository contractRepository;

    @Autowired
    private BaladiayaRepository baladiayaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // Alter column length for status in MySQL to avoid Data truncated errors
        try {
            jdbcTemplate.execute("ALTER TABLE issue_reports MODIFY COLUMN status VARCHAR(50)");
            jdbcTemplate.execute("ALTER TABLE marriage_contracts MODIFY COLUMN status VARCHAR(50)");
            jdbcTemplate.execute("ALTER TABLE document_requests MODIFY COLUMN status VARCHAR(50)");
        } catch (Exception ignored) {}

        // Ensure Baladiyat are always seeded/updated with latest schedules
        seedBaladiyat();

        if (userRepository.count() > 0) {
            return; // Database users already seeded
        }

        // 1. Seed Municipal Agent
        User agent = User.builder()
                .name("أمين بلدي (مروان التونسي)")
                .email("agent@baladiati.gov.tn")
                .password(passwordEncoder.encode("agent123"))
                .role(Role.ROLE_AGENT)
                .phone("22334455")
                .nationalId("08888888")
                .isActive(true)
                .activationCode("111111") // Identifiant unique pour le login Agent
                .build();
        userRepository.save(agent);

        // 2. Seed Marriage Officiant
        User officiant = User.builder()
                .name("عدل منفذ")
                .email("officiant@baladiati.gov.tn")
                .password(passwordEncoder.encode("officiant123"))
                .role(Role.ROLE_OFFICIANT)
                .phone("33445566")
                .nationalId("09999999")
                .isActive(true)
                .activationCode("222222") // Identifiant unique pour le login Officier
                .build();
        userRepository.save(officiant);

        // 3. Seed Citizens
        User citizen1 = User.builder()
                .name("xxxxx xxxxxxx")
                .email("citizen@gmail.com")
                .password(passwordEncoder.encode("citizen123"))
                .role(Role.ROLE_CITIZEN)
                .phone("33333333")
                .nationalId("01234567")
                .build();
        userRepository.save(citizen1);

        User citizen2 = User.builder()
                .name("فاطمة الورغي")
                .email("fatma@gmail.com")
                .password(passwordEncoder.encode("citizen123"))
                .role(Role.ROLE_CITIZEN)
                .phone("99112233")
                .nationalId("07777777")
                .build();
        userRepository.save(citizen2);

        // 4. Seed Document Requests
        DocumentRequest req1 = DocumentRequest.builder()
                .type("مضمون ولادة (Extrait de Naissance)")
                .status(RequestStatus.READY)
                .details("مضمون ولادة خاص بأحمد بن علي، مواليد 12/05/1995 بتونس")
                .creationDate(LocalDateTime.now().minusDays(5))
                .citizen(citizen1)
                .build();
        
        DocumentRequest req2 = DocumentRequest.builder()
                .type("شهادة إقامة (Certificat de Résidence)")
                .status(RequestStatus.VERIFYING)
                .details("شهادة إقامة - العنوان: 12 نهج الحرية، تونس 1002")
                .creationDate(LocalDateTime.now().minusDays(2))
                .citizen(citizen1)
                .attachments(new ArrayList<>(Arrays.asList(
                    Attachment.builder()
                            .fileName("facture_steg.pdf")
                            .fileType("application/pdf")
                            .fileUrl("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf")
                            .build()
                )))
                .build();

        DocumentRequest req3 = DocumentRequest.builder()
                .type("رخصة بناء (Permis de Construire)")
                .status(RequestStatus.INCOMPLETE)
                .details("رخصة بناء ملحق علوي للمسكن الكائن بحي الخضراء")
                .creationDate(LocalDateTime.now().minusDays(10))
                .citizen(citizen2)
                .missingDocNotes("الرجاء إرفاق شهادة الملكية مصورة بوضوح")
                .attachments(new ArrayList<>(Arrays.asList(
                    Attachment.builder()
                            .fileName("plan_architectural.jpg")
                            .fileType("image/jpeg")
                            .fileUrl("https://placehold.co/600x400")
                            .build()
                )))
                .build();

        requestRepository.saveAll(Arrays.asList(req1, req2, req3));

        // 5. Seed Issue Reports
        IssueReport rep1 = IssueReport.builder()
                .type("GARBAGE")
                .description("حاوية القمامة لم يقع رفعها منذ 3 أيام وتسببت في روائح كريهة في نهج الحرية.")
                .photoUrl("https://placehold.co/600x400/orange/white?text=Garbage+Issue")
                .latitude(36.8065)
                .longitude(10.1815)
                .status(ReportStatus.RECEIVED)
                .creationDate(LocalDateTime.now().minusDays(3))
                .citizen(citizen1)
                .build();

        IssueReport rep2 = IssueReport.builder()
                .type("LIGHTING")
                .description("عمود إنارة معطل بالكامل مما يجعل الطريق مظلماً وخطيراً ليلاً.")
                .photoUrl("https://placehold.co/600x400/navy/white?text=Broken+Lighting")
                .latitude(36.8188)
                .longitude(10.1659)
                .status(ReportStatus.INVESTIGATING)
                .creationDate(LocalDateTime.now().minusDays(1))
                .citizen(citizen2)
                .build();

        reportRepository.saveAll(Arrays.asList(rep1, rep2));

        // 6. Seed Marriage Contracts
        MarriageContract mc1 = MarriageContract.builder()
                .groomName("ياسين العياري")
                .brideName("منى القاسمي")
                .contractDate(LocalDate.now().minusDays(1))
                .place("قاعة بلدية سيدي بوسعيد")
                .witnessDetails("الشاهد 1: سليم العياري (بطاقة تعريف: 02345678)، الشاهد 2: قيس القاسمي (بطاقة تعريف: 03456789)")
                .status(ContractStatus.PENDING_REVIEW)
                .creationDate(LocalDateTime.now().minusHours(12))
                .officiant(officiant)
                .attachments(new ArrayList<>(Arrays.asList(
                    Attachment.builder()
                            .fileName("cin_epoux.pdf")
                            .fileType("application/pdf")
                            .fileUrl("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf")
                            .build()
                )))
                .build();

        contractRepository.save(mc1);

        // 7. Seed Baladiyat (municipalities)
        seedBaladiyat();
        
        System.out.println(">>> Database Seeded Successfully with default accounts and mock data!");
    }

    private void seedBaladiyat() {
        baladiayaRepository.deleteAll(); // Clean and fresh accurate dataset

        List<Baladiya> baladiyat = List.of(
            // ── 1. GOUVERNORAT DE TUNIS ──
            Baladiya.builder().name("بلدية تونس (القصبة) / Mairie de Tunis (La Kasbah)").nameAr("بلدية تونس (القصبة)").nameFr("Mairie de Tunis (La Kasbah)")
                .address("Place de la Kasbah, Tunis").latitude(36.7964).longitude(10.1706).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(5).build(),
            Baladiya.builder().name("بلدية المنزه / Mairie d'El Menzah").nameAr("بلدية المنزه").nameFr("Mairie d'El Menzah")
                .address("Avenue d'Afrique, El Menzah 5, Tunis").latitude(36.8361).longitude(10.1754).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(4).build(),
            Baladiya.builder().name("بلدية العمران / Mairie d'El Omrane").nameAr("بلدية العمران").nameFr("Mairie d'El Omrane")
                .address("Rue 4140, El Omrane, Tunis").latitude(36.8220).longitude(10.1620).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(7).build(),
            Baladiya.builder().name("بلدية باردو / Mairie du Bardo").nameAr("بلدية باردو").nameFr("Mairie du Bardo")
                .address("Avenue 20 Mars, Le Bardo, Tunis").latitude(36.8092).longitude(10.1412).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(6).build(),
            Baladiya.builder().name("بلدية الكرم / Mairie du Kram").nameAr("بلدية الكرم").nameFr("Mairie du Kram")
                .address("Avenue Habib Bourguiba, Le Kram").latitude(36.8333).longitude(10.3167).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(3).build(),
            Baladiya.builder().name("بلدية حلق الوادي / Mairie de La Goulette").nameAr("بلدية حلق الوادي").nameFr("Mairie de La Goulette")
                .address("Avenue Franklin Roosevelt, La Goulette").latitude(36.8180).longitude(10.3050).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(2).build(),
            Baladiya.builder().name("بلدية قرطاج / Mairie de Carthage").nameAr("بلدية قرطاج").nameFr("Mairie de Carthage")
                .address("Rue Hannibal, Carthage").latitude(36.8525).longitude(10.3240).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(1).build(),
            Baladiya.builder().name("بلدية سيدي بوسعيد / Mairie de Sidi Bou Saïd").nameAr("بلدية سيدي بوسعيد").nameFr("Mairie de Sidi Bou Saïd")
                .address("Rue Habib Thameur, Sidi Bou Saïd").latitude(36.8700).longitude(10.3417).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(2).build(),
            Baladiya.builder().name("بلدية المرسى / Mairie de La Marsa").nameAr("بلدية المرسى").nameFr("Mairie de La Marsa")
                .address("Avenue Habib Bourguiba, La Marsa").latitude(36.8784).longitude(10.3248).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(8).build(),
            Baladiya.builder().name("بلدية سيدي حسين / Mairie de Sidi Hassine").nameAr("بلدية سيدي حسين").nameFr("Mairie de Sidi Hassine")
                .address("Route de Fouchana, Sidi Hassine").latitude(36.7583).longitude(10.1264).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(9).build(),

            // ── 2. GOUVERNORAT DE L'ARIANA ──
            Baladiya.builder().name("بلدية أريانة / Mairie d'Ariana").nameAr("بلدية أريانة").nameFr("Mairie d'Ariana")
                .address("Avenue Habib Bourguiba, Ariana").latitude(36.8580).longitude(10.1947).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(6).build(),
            Baladiya.builder().name("بلدية التضامن / Mairie d me Ettadhamen").nameAr("بلدية التضامن").nameFr("Mairie d'Ettadhamen")
                .address("Avenue 14 Janvier, Cité Ettadhamen").latitude(36.8406).longitude(10.1234).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(11).build(),
            Baladiya.builder().name("بلدية المنيهلة / Mairie de Mnihla").nameAr("بلدية المنيهلة").nameFr("Mairie de Mnihla")
                .address("Route de Bizerte, Mnihla").latitude(36.8622).longitude(10.1089).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(5).build(),
            Baladiya.builder().name("بلدية رواد / Mairie de Raoued").nameAr("بلدية رواد").nameFr("Mairie de Raoued")
                .address("Route Jaafar, Raoued").latitude(36.9031).longitude(10.1972).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(7).build(),
            Baladiya.builder().name("بلدية سكرة / Mairie de Soukra").nameAr("بلدية سكرة").nameFr("Mairie de Soukra")
                .address("Avenue de l'U.M.A, La Soukra").latitude(36.8856).longitude(10.2528).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(10).build(),
            Baladiya.builder().name("بلدية قلعة الأندلس / Mairie de Kalâat el-Andalous").nameAr("بلدية قلعة الأندلس").nameFr("Mairie de Kalâat el-Andalous")
                .address("Centre Ville, Kalâat el-Andalous").latitude(37.0622).longitude(10.1161).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(1).build(),

            // ── 3. GOUVERNORAT DE BEN AROUS ──
            Baladiya.builder().name("بلدية بن عروس / Mairie de Ben Arous").nameAr("بلدية بن عروس").nameFr("Mairie de Ben Arous")
                .address("Avenue Habib Bourguiba, Ben Arous").latitude(36.7530).longitude(10.2225).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(4).build(),
            Baladiya.builder().name("بلدية رادس / Mairie de Radès").nameAr("بلدية رادس").nameFr("Mairie de Radès")
                .address("Avenue de la République, Radès").latitude(36.7700).longitude(10.2764).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(3).build(),
            Baladiya.builder().name("بلدية الزهراء / Mairie d'Ezzahra").nameAr("بلدية الزهراء").nameFr("Mairie d'Ezzahra")
                .address("Avenue 14 Janvier, Ezzahra").latitude(36.7444).longitude(10.3083).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(5).build(),
            Baladiya.builder().name("بلدية حمام الأنف / Mairie de Hammam-Lif").nameAr("بلدية حمام الأنف").nameFr("Mairie de Hammam-Lif")
                .address("Rue Farhat Hached, Hammam-Lif").latitude(36.7289).longitude(10.3392).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(6).build(),
            Baladiya.builder().name("بلدية حمام الشط / Mairie de Hammam Chott").nameAr("بلدية حمام الشط").nameFr("Mairie de Hammam Chott")
                .address("Avenue Taïeb Mehiri, Hammam Chott").latitude(36.6975).longitude(10.3667).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(2).build(),
            Baladiya.builder().name("بلدية المروج / Mairie d'El Mourouj").nameAr("بلدية المروج").nameFr("Mairie d'El Mourouj")
                .address("Avenue de la الشهداء, El Mourouj 1").latitude(36.7328).longitude(10.2078).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(12).build(),
            Baladiya.builder().name("بلدية فوشانة / Mairie de Fouchana").nameAr("بلدية فوشانة").nameFr("Mairie de Fouchana")
                .address("Centre Ville, Fouchana").latitude(36.6917).longitude(10.1744).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(4).build(),
            Baladiya.builder().name("بلدية المحمدية / Mairie de Mohamedia").nameAr("بلدية المحمدية").nameFr("Mairie de Mohamedia")
                .address("Route de Zaghouan, Mohamedia").latitude(36.6744).longitude(10.1558).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(5).build(),
            Baladiya.builder().name("بلدية مرناق / Mairie de Mornag").nameAr("بلدية مرناق").nameFr("Mairie de Mornag")
                .address("Avenue Habib Bourguiba, Mornag").latitude(36.6792).longitude(10.2889).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(3).build(),

            // ── 4. GOUVERNORAT DE LA MANOUBA ──
            Baladiya.builder().name("بلدية المنوبة / Mairie de Manouba").nameAr("بلدية المنوبة").nameFr("Mairie de Manouba")
                .address("Avenue UMA, Manouba").latitude(36.8085).longitude(10.0965).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(5).build(),
            Baladiya.builder().name("بلدية الدندان / Mairie de Denden").nameAr("بلدية الدندان").nameFr("Mairie de Denden")
                .address("Avenue l'Indépendance, Denden").latitude(36.8011).longitude(10.1136).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(4).build(),
            Baladiya.builder().name("بلدية طبربة / Mairie de Tebourba").nameAr("بلدية طبربة").nameFr("Mairie de Tebourba")
                .address("Centre Ville, Tebourba").latitude(36.8297).longitude(9.8406).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(2).build(),
            Baladiya.builder().name("بلدية وادي الليل / Mairie de Oued Ellil").nameAr("بلدية وادي الليل").nameFr("Mairie de Oued Ellil")
                .address("Route de Mateur, Oued Ellil").latitude(36.8378).longitude(10.0417).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(6).build(),
            Baladiya.builder().name("بلدية المرناقية / Mairie de Mornaguia").nameAr("بلدية المرناقية").nameFr("Mairie de Mornaguia")
                .address("Avenue Habib Bourguiba, Mornaguia").latitude(36.7581).longitude(10.0189).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(3).build(),

            // ── 5. GOUVERNORAT DE NABEUL (CAP BON) ──
            Baladiya.builder().name("بلدية نابل / Mairie de Nabeul").nameAr("بلدية نابل").nameFr("Mairie de Nabeul")
                .address("Avenue Habib Bourguiba, Nabeul").latitude(36.4561).longitude(10.7376).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(4).build(),
            Baladiya.builder().name("بلدية الحمامات / Mairie de Hammamet").nameAr("بلدية الحمامات").nameFr("Mairie de Hammamet")
                .address("Avenue Habib Bourguiba, Hammamet").latitude(36.4000).longitude(10.6167).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(7).build(),
            Baladiya.builder().name("بلدية منزل تميم / Mairie de Menzel Temime").nameAr("بلدية منزل تميم").nameFr("Mairie de Menzel Temime")
                .address("Avenue l'Indépendance, Menzel Temime").latitude(36.7828).longitude(10.9903).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(3).build(),
            Baladiya.builder().name("بلدية قStructure قليبية / Mairie de Kelibia").nameAr("بلدية قليبية").nameFr("Mairie de Kelibia")
                .address("Avenue الشهداء, Kelibia").latitude(36.8475).longitude(11.0939).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(5).build(),
            Baladiya.builder().name("بلدية قرمبالية / Mairie de Grombalia").nameAr("بلدية قرمبالية").nameFr("Mairie de Grombalia")
                .address("Place du Maghreb Arabe, Grombalia").latitude(36.5986).longitude(10.4983).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(2).build(),
            Baladiya.builder().name("بلدية سليمان / Mairie de Soliman").nameAr("بلدية سليمان").nameFr("Mairie de Soliman")
                .address("Avenue Habib Bourguiba, Soliman").latitude(36.6944).longitude(10.4908).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(4).build(),
            Baladiya.builder().name("بلدية كوربة / Mairie de Korba").nameAr("بلدية كوربة").nameFr("Mairie de Korba")
                .address("Avenue Habib Bourguiba, Korba").latitude(36.5772).longitude(10.8586).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(3).build(),
            Baladiya.builder().name("بلدية الهوارية / Mairie d'El Haouaria").nameAr("بلدية الهوارية").nameFr("Mairie d'El Haouaria")
                .address("Avenue 20 Mars, El Haouaria").latitude(37.0500).longitude(11.0167).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(1).build(),

            // ── 6. GOUVERNORAT DE BIZERTE ──
            Baladiya.builder().name("بلدية بنزرت / Mairie de Bizerte").nameAr("بلدية بنزرت").nameFr("Mairie de Bizerte")
                .address("Boulevard Hassan Nouri, Bizerte").latitude(37.2744).longitude(9.8739).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(9).build(),
            Baladiya.builder().name("بلدية منزل بورقيبة / Mairie de Menzel Bourguiba").nameAr("بلدية منزل بورقيبة").nameFr("Mairie de Menzel Bourguiba")
                .address("Place de la Libération, Menzel Bourguiba").latitude(37.1536).longitude(9.7858).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(6).build(),
            Baladiya.builder().name("بلدية رأس الجبل / Mairie de Ras Jebel").nameAr("بلدية رأس الجبل").nameFr("Mairie de Ras Jebel")
                .address("Avenue Habib Bourguiba, Ras Jebel").latitude(37.2153).longitude(10.1214).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(3).build(),
            Baladiya.builder().name("بلدية ماطر / Mairie de Mateur").nameAr("بلدية ماطر").nameFr("Mairie de Mateur")
                .address("Avenue l'Indépendance, Mateur").latitude(37.0400).longitude(9.6650).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(4).build(),
            Baladiya.builder().name("بلدية طبرقة / Mairie de Tabarka").nameAr("بلدية طبرقة").nameFr("Mairie de Tabarka")
                .address("Avenue Habib Bourguiba, Tabarka").latitude(36.9544).longitude(8.7581).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(2).build(),

            // ── 7. NORD-OUEST (BÉJA, JENDOUBA, KEF, SILIANA, ZAGHOUAN) ──
            Baladiya.builder().name("بلدية باجة / Mairie de Béja").nameAr("بلدية باجة").nameFr("Mairie de Béja")
                .address("Avenue Habib Bourguiba, Béja").latitude(36.7256).longitude(9.1817).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(3).build(),
            Baladiya.builder().name("بلدية مجاز الباب / Mairie de Medjez el-Bab").nameAr("بلدية مجاز الباب").nameFr("Mairie de Medjez el-Bab")
                .address("Avenue de la République, Medjez el-Bab").latitude(36.6489).longitude(9.6083).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(2).build(),
            Baladiya.builder().name("بلدية جندوبة / Mairie de Jendouba").nameAr("بلدية جندوبة").nameFr("Mairie de Jendouba")
                .address("Avenue Habib Bourguiba, Jendouba").latitude(36.5011).longitude(8.7802).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(4).build(),
            Baladiya.builder().name("بلدية عين دراهم / Mairie d'Aïn Draham").nameAr("بلدية عين دراهم").nameFr("Mairie d'Aïn Draham")
                .address("Avenue 7 Novembre, Aïn Draham").latitude(36.7761).longitude(8.6881).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(1).build(),
            Baladiya.builder().name("بلدية الكاف / Mairie du Kef").nameAr("بلدية الكاف").nameFr("Mairie du Kef")
                .address("Place de l'Indépendance, Le Kef").latitude(36.1822).longitude(8.7148).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(3).build(),
            Baladiya.builder().name("بلدية سليانة / Mairie de Siliana").nameAr("بلدية سليانة").nameFr("Mairie de Siliana")
                .address("Avenue Habib Bourguiba, Siliana").latitude(36.0849).longitude(9.3708).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(2).build(),
            Baladiya.builder().name("بلدية مكثر / Mairie de Makthar").nameAr("بلدية مكثر").nameFr("Mairie de Makthar")
                .address("Avenue Habib Bourguiba, Makthar").latitude(35.8581).longitude(9.2069).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(1).build(),
            Baladiya.builder().name("بلدية زغوان / Mairie de Zaghouan").nameAr("بلدية زغوان").nameFr("Mairie de Zaghouan")
                .address("Avenue l'Indépendance, Zaghouan").latitude(36.4029).longitude(10.1429).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(3).build(),

            // ── 8. SAHEL & CENTRE (SOUSSE, MONASTIR, MAHDIA, SFAX, KAIROUAN, KASSERINE, SIDI BOUZID) ──
            Baladiya.builder().name("بلدية سوسة / Mairie de Sousse").nameAr("بلدية سوسة").nameFr("Mairie de Sousse")
                .address("Boulevard Mohamed V, Sousse").latitude(35.8256).longitude(10.6411).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(11).build(),
            Baladiya.builder().name("بلدية حمام سوسة / Mairie de Hammam Sousse").nameAr("بلدية حمام سوسة").nameFr("Mairie de Hammam Sousse")
                .address("Avenue Habib Bourguiba, Hammam Sousse").latitude(35.8589).longitude(10.5939).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(6).build(),
            Baladiya.builder().name("بلدية مساكن / Mairie de Msaken").nameAr("بلدية مساكن").nameFr("Mairie de Msaken")
                .address("Avenue 14 Janvier, Msaken").latitude(35.7297).longitude(10.5808).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(5).build(),
            Baladiya.builder().name("بلدية المنستير / Mairie de Monastir").nameAr("بلدية المنستير").nameFr("Mairie de Monastir")
                .address("Avenue Habib Bourguiba, Monastir").latitude(35.7833).longitude(10.8333).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(5).build(),
            Baladiya.builder().name("بلدية المكنين / Mairie de Moknine").nameAr("بلدية المكنين").nameFr("Mairie de Moknine")
                .address("Avenue Habib Bourguiba, Moknine").latitude(35.6264).longitude(10.9006).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(4).build(),
            Baladiya.builder().name("بلدية قصر هلال / Mairie de Ksar Hellal").nameAr("بلدية قصر هلال").nameFr("Mairie de Ksar Hellal")
                .address("Avenue Habib Bourguiba, Ksar Hellal").latitude(35.6472).longitude(10.8911).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(4).build(),
            Baladiya.builder().name("بلدية المهدية / Mairie de Mahdia").nameAr("بلدية المهدية").nameFr("Mairie de Mahdia")
                .address("Avenue 14 Janvier, Mahdia").latitude(35.5047).longitude(11.0622).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(4).build(),
            Baladiya.builder().name("بلدية الجم / Mairie d'El Jem").nameAr("بلدية الجم").nameFr("Mairie d'El Jem")
                .address("Avenue de la République, El Jem").latitude(35.3000).longitude(10.7167).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(2).build(),
            Baladiya.builder().name("بلدية صفاقس (باب بحر) / Mairie de Sfax (Bab Bhar)").nameAr("بلدية صفاقس (باب بحر)").nameFr("Mairie de Sfax (Bab Bhar)")
                .address("Avenue Habib Bourguiba, Sfax").latitude(34.7406).longitude(10.7603).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(15).build(),
            Baladiya.builder().name("بلدية ساقية الزيت / Mairie de Sakiet Ezzit").nameAr("بلدية ساقية الزيت").nameFr("Mairie de Sakiet Ezzit")
                .address("Route de Tunis Km 6, Sakiet Ezzit, Sfax").latitude(34.7958).longitude(10.7733).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(7).build(),
            Baladiya.builder().name("بلدية القيروان / Mairie de Kairouan").nameAr("بلدية القيروان").nameFr("Mairie de Kairouan")
                .address("Avenue Habib Bourguiba, Kairouan").latitude(35.6781).longitude(10.0963).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(8).build(),
            Baladiya.builder().name("بلدية القصرين / Mairie de Kasserine").nameAr("بلدية القصرين").nameFr("Mairie de Kasserine")
                .address("Avenue Habib Bourguiba, Kasserine").latitude(35.1676).longitude(8.8365).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(5).build(),
            Baladiya.builder().name("بلدية سيدي بوزيد / Mairie de Sidi Bouzid").nameAr("بلدية سيدي بوزيد").nameFr("Mairie de Sidi Bouzid")
                .address("Avenue Principal, Sidi Bouzid").latitude(35.0382).longitude(9.4849).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(4).build(),

            // ── 9. SUD (GAFSA, TOZEUR, KEBILI, GABÈS, MÉDENINE, DJERBA, TATAOUINE) ──
            Baladiya.builder().name("بلدية قفصة / Mairie de Gafsa").nameAr("بلدية قفصة").nameFr("Mairie de Gafsa")
                .address("Avenue Habib Bourguiba, Gafsa").latitude(34.4250).longitude(8.7842).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(7).build(),
            Baladiya.builder().name("بلدية توزر / Mairie de Tozeur").nameAr("بلدية توزر").nameFr("Mairie de Tozeur")
                .address("Avenue Aboulkacem Chebbi, Tozeur").latitude(33.9197).longitude(8.1336).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(3).build(),
            Baladiya.builder().name("بلدية نطة / Mairie de Nefta").nameAr("بلدية نفطة").nameFr("Mairie de Nefta")
                .address("Avenue Habib Bourguiba, Nefta").latitude(33.8731).longitude(7.8778).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(1).build(),
            Baladiya.builder().name("بلدية قبلي / Mairie de Kebili").nameAr("بلدية قبلي").nameFr("Mairie de Kebili")
                .address("Avenue Habib Bourguiba, Kebili").latitude(33.7050).longitude(8.9650).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(2).build(),
            Baladiya.builder().name("بلدية دوز / Mairie de Douz").nameAr("بلدية دوز").nameFr("Mairie de Douz")
                .address("Avenue des Martyrs, Douz").latitude(33.4664).longitude(9.0203).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(2).build(),
            Baladiya.builder().name("بلدية قابس / Mairie de Gabès").nameAr("بلدية قابس").nameFr("Mairie de Gabès")
                .address("Avenue Farhat Hached, Gabès").latitude(33.8814).longitude(10.0982).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(9).build(),
            Baladiya.builder().name("بلدية الحامة / Mairie d'El Hamma").nameAr("بلدية الحامة").nameFr("Mairie d'El Hamma")
                .address("Avenue Habib Bourguiba, El Hamma").latitude(33.8858).longitude(9.7958).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(3).build(),
            Baladiya.builder().name("بلدية مدنين / Mairie de Médenine").nameAr("بلدية مدنين").nameFr("Mairie de Médenine")
                .address("Avenue Habib Bourguiba, Médenine").latitude(33.3547).longitude(10.4925).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(4).build(),
            Baladiya.builder().name("بلدية جربة حومة السوق / Mairie de Djerba Houmt Souk").nameAr("بلدية جربة حومة السوق").nameFr("Mairie de Djerba Houmt Souk")
                .address("Avenue Habib Bourguiba, Houmt Souk Djerba").latitude(33.8750).longitude(10.8580).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(6).build(),
            Baladiya.builder().name("بلدية جربة ميدون / Mairie de Djerba Midoun").nameAr("بلدية جربة ميدون").nameFr("Mairie de Djerba Midoun")
                .address("Centre Ville, Midoun Djerba").latitude(33.8081).longitude(10.9922).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(5).build(),
            Baladiya.builder().name("بلدية جرجيس / Mairie de Zarzis").nameAr("بلدية جرجيس").nameFr("Mairie de Zarzis")
                .address("Avenue 14 Janvier, Zarzis").latitude(33.5039).longitude(11.1122).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(5).build(),
            Baladiya.builder().name("بلدية تطاوين / Mairie de Tataouine").nameAr("بلدية تطاوين").nameFr("Mairie de Tataouine")
                .address("Avenue Habib Bourguiba, Tataouine").latitude(32.9297).longitude(10.4518).openingTime("08:00").closingTime("17:00").workingDays("Lun-Ven").queueCount(3).build()
        );

        baladiayaRepository.saveAll(baladiyat);
        System.out.println("✅ Clean dataset re-seeded with " + baladiyat.size() + " official municipalities covering all 24 governorates of Tunisia!");
    }
}
