package com.example.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;

import java.nio.file.Path;
import java.util.Collection;

public class ReadFiles {

    private static final String INDEX_PATH =
            "C:/Users/LOKESH/Git/LearnLuceneAgain/index";

    public static void main(String[] args) throws Exception {

        Directory dir = FSDirectory.open(Path.of(INDEX_PATH));
        DirectoryReader reader = DirectoryReader.open(dir);

        System.out.println("========================================");
        System.out.println("LUCENE INDEX FILES – FULL VIEW");
        System.out.println("========================================");

        /* =================================================
         * 1️⃣ segments_N (Commit metadata)
         * ================================================= */
        System.out.println("\n📘 segments_N (Commit file)");
        SegmentInfos segmentInfos = SegmentInfos.readLatestCommit(dir);

        System.out.println("Commit generation : " + segmentInfos.getGeneration());
        System.out.println("Lucene version    : " + segmentInfos.getCommitLuceneVersion());
        System.out.println("User data         : " + segmentInfos.getUserData());

        /* =================================================
         * 2️⃣ Per-segment details
         * ================================================= */
        for (LeafReaderContext ctx : reader.leaves()) {

            SegmentReader segReader = (SegmentReader) ctx.reader();
            SegmentCommitInfo commitInfo = segReader.getSegmentInfo();
            SegmentInfo info = commitInfo.info;

            System.out.println("\n========================================");
            System.out.println("SEGMENT : " + info.name);
            System.out.println("========================================");

            /* ---------------- _X.si ---------------- */
            System.out.println("\n📄 _X.si (Segment Info)");
            System.out.println("Segment name       : " + info.name);
            System.out.println("Max docs           : " + info.maxDoc());
            System.out.println("Lucene version     : " + info.getVersion());
            System.out.println("Codec              : " + info.getCodec().getName());
            System.out.println("Uses compound file : " + info.getUseCompoundFile());
            System.out.println("Diagnostics        : " + info.getDiagnostics());

            /* ---------------- _X.cfe ---------------- */
            System.out.println("\n📘 _X.cfe (Compound File Entries)");
            if (info.getUseCompoundFile()) {

                Collection<String> files = commitInfo.files();

                for (String f : files) {
                    if (f.endsWith(".cfe")) {
                        System.out.println("CFE file : " + f);
                    }
                }

                System.out.println("Files indexed by CFE:");
                for (String f : files) {
                    if (!f.endsWith(".cfs") && !f.endsWith(".cfe")) {
                        System.out.println("  - " + f);
                    }
                }
            }

            /* ---------------- _X.cfs ---------------- */
            System.out.println("\n📦 _X.cfs (Compound File Storage)");
            if (info.getUseCompoundFile()) {
                for (String f : commitInfo.files()) {
                    if (f.endsWith(".cfs")) {
                        System.out.println("CFS file : " + f);
                    }
                }
            }

            /* ---------------- Stored Fields ---------------- */
            System.out.println("\n📄 STORED FIELDS");
            Bits liveDocs = segReader.getLiveDocs();

            for (int docId = 0; docId < segReader.maxDoc(); docId++) {
                if (liveDocs != null && !liveDocs.get(docId)) continue;

                Document doc = segReader.storedFields().document(docId);
                System.out.println("DocID " + docId + " => " + doc);
            }

            /* ---------------- Norms ---------------- */
            System.out.println("\n📊 NORMS");
            for (FieldInfo fi : segReader.getFieldInfos()) {
                if (fi.hasNorms()) {
                    System.out.println("Field with norms: " + fi.name);
                }
            }

            /* ---------------- Terms / Postings / Positions ---------------- */
            System.out.println("\n🔎 TERMS / POSTINGS / POSITIONS");

            for (FieldInfo fi : segReader.getFieldInfos()) {

                if (fi.getIndexOptions() == IndexOptions.NONE)
                    continue;

                Terms terms = segReader.terms(fi.name);
                if (terms == null) continue;

                TermsEnum te = terms.iterator();
                BytesRef term;

                while ((term = te.next()) != null) {

                    PostingsEnum pe =
                            te.postings(null, PostingsEnum.POSITIONS);

                    while (pe.nextDoc() != PostingsEnum.NO_MORE_DOCS) {

                        System.out.print(
                                "Field=" + fi.name +
                                        " Term=" + term.utf8ToString() +
                                        " DocID=" + pe.docID() +
                                        " Freq=" + pe.freq() +
                                        " Pos=[ "
                        );

                        for (int i = 0; i < pe.freq(); i++) {
                            System.out.print(pe.nextPosition() + " ");
                        }
                        System.out.println("]");
                    }
                }
            }

            /* ---------------- Live Docs ---------------- */
            System.out.println("\n🗑 LIVE DOCS");
            if (liveDocs == null) {
                System.out.println("No deleted documents in this segment.");
            } else {
                for (int i = 0; i < segReader.maxDoc(); i++) {
                    System.out.println("Doc " + i + " alive = " + liveDocs.get(i));
                }
            }
        }

        /* =================================================
         * 3️⃣ write.lock — Writer lock
         * ================================================= */
        System.out.println("\n🔒 write.lock");
        try {
            dir.obtainLock(IndexWriter.WRITE_LOCK_NAME);
            System.out.println("Index is NOT locked (no active writer)");
        } catch (LockObtainFailedException e) {
            System.out.println("Index IS locked (writer active)");
        }

        reader.close();
        dir.close();

        System.out.println("\n✅ DONE");
    }
}