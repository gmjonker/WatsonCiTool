package a2t.citool;

import com.ibm.watson.developer_cloud.concept_insights.v2.ConceptInsights;
import com.ibm.watson.developer_cloud.concept_insights.v2.model.*;
import util.LambdaLogger;

import java.util.*;
import java.util.stream.Collectors;

import static util.Util.containsNormalized;
import static util.Util.map;

public class CiCorpusHelper
{
//    private static ConceptInsights conceptInsightsService = CiDefaults.getConceptInsightsService();

    private static final LambdaLogger log = new LambdaLogger(CiCorpusHelper.class);

    public static Corpus createNewCorpus(ConceptInsights conceptInsightsService, String accountId, String corpusName)
    {
        log.info("Creating new corpus {}/{}", accountId, corpusName);
        Corpus corpus = new Corpus(accountId, corpusName);
        conceptInsightsService.createCorpus(corpus);
        log.info("Corpus created");
        return corpus;
    }

    public static void getAccountsInfo(ConceptInsights conceptInsightsService)
    {
        Accounts accounts = conceptInsightsService.getAccountsInfo();
        log.info("accounts = " + accounts);
        String accountId = accounts.getAccounts().get(0).getId();
        log.info("accountId = " + accountId);
    }

//    public static void getCorpiInfo(ConceptInsights conceptInsightsService)
//    {
//        Corpora corpora = conceptInsightsService.listCorpora(CiDefaults.getAccountId());
//        for (Corpus corpus : corpora.getCorpora())
//            log.info("corpus.getId() = " + corpus.getId());
//        String corpusId = corpora.getCorpora().get(0).getId();
//    }

    public static Set<String> getAllDocumentIds(ConceptInsights conceptInsightsService, Corpus corpus)
    {
        log.debug("Getting all documents from CI corpus '{}'...", CiUtil.getNameFromId(corpus.getId()));
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ConceptInsights.LIMIT, 0); // 0 will get the maximum of 100.000 documents
        List<String> documentIds = conceptInsightsService.listDocuments(corpus, parameters).getDocuments();
        log.trace("documentIds = {}", () -> documentIds);
        if (documentIds.size() == 100000)
            log.warn("Received 100000 documents from CI. This indicates that there are more than 100000 documents," +
                    "so we should fetch documents incrementally.");
        return new HashSet<>(documentIds);
    }

    public static Set<String> getAllDocumentNames(ConceptInsights conceptInsightsService, Corpus corpus)
    {
        return map(getAllDocumentIds(conceptInsightsService, corpus), CiUtil::getNameFromId);
    }

    public static Set<Document> getAllDocuments(ConceptInsights conceptInsightsService, Corpus corpus)
    {
        return map(getAllDocumentIds(conceptInsightsService, corpus), id -> CiUtil.getDocumentFromId(id, corpus));
    }

    public static List<Document> findDocumentsByPartialName(ConceptInsights conceptInsightsService, Corpus corpus, String query)
    {
        return getAllDocumentIds(conceptInsightsService, corpus).stream()
                .filter(
                        id -> containsNormalized(CiUtil.getNameFromId(id), query)
                )
                .map(
                        id -> CiUtil.getDocumentFromId(id, corpus)
                )
                .collect(Collectors.toList());
    }

//    public static List<CiDocumentResult> documentResultsToCiResults(List<MatchedDocument> matchedDocuments)
//    {
//        return matchedDocuments.stream()
//                .map(documentResult -> {
//                            List<Annotation> annotations = documentResult.getAnnotations();
//                            String explanation = "";
//                            List<List<Integer>> textIndices = new ArrayList<>();
//                            for (Annotation annotation : annotations) {
//                                explanation += String.format("%s (%.2f), ", annotation.getConcept().getLabel(),
//                                        annotation.getScore());
//                                textIndices.add(annotation.getTextIndex());
//                            }
//                            return new CiDocumentResult(
//                                    CiUtil.getNameFromId(documentResult.getId()),
//                                    documentResult.getLabel(),
//                                    "?",
//                                    documentResult.getScore(),
//                                    "?",
//                                    explanation,
//                                    textIndices,
//                                    "?"
//                            );
//                        }
//                )
//                .collect(Collectors.toList());
//    }

    static void enrich(ConceptInsights conceptInsightsService, Document document)
    {
        Document fullDocument = conceptInsightsService.getDocument(document);
        assert Objects.equals(document.getId(), fullDocument.getId());
        assert Objects.equals(document.getName(), fullDocument.getName());
        document.setExpiresOn(fullDocument.getExpiresOn());
        document.setLabel(fullDocument.getLabel());
        document.setLastModified(fullDocument.getLastModified());
        document.setParts(fullDocument.getParts());
        document.setTimeToLive(fullDocument.getTimeToLive());
        document.setUserFields(fullDocument.getUserFields());
    }

//    /**
//     * Call after findDocumentsByConcepts
//     */
//    static void explain(Concept searchedConcept, MatchedDocument matchedDocument)
//    {
//        log.debug("Explaining the fact that {} was a result when searching for concept '{}'", matchedDocument.getLabel(), searchedConcept.getName());
//        log.debug("Document match of {} with '{}' is {}", matchedDocument.getLabel(), searchedConcept.getName(), matchedDocument.getScore());
//        List<Annotation> annotations = matchedDocument.getAnnotations();
//        for (Annotation annotation : annotations) {
////            log.debug("Annotation score is {}", annotation.score);
//            Concept foundConcept = annotation.getConcept();
////            System.out.println("foundConcept = " + foundConcept);
//            log.debug("Found concept '{}' in text, in part {}, text index {}, and score is {}", foundConcept.getLabel(),
//                    annotation.getPartsIndex(), annotation.getTextIndex(), annotation.getScore());
//            // Query the relation of this found concept with the searched for concept
//            Scores scores = CiGraphQuerier.getInstance().getGraphRelationScores(searchedConcept, Arrays.asList(foundConcept.getId()));
//            log.debug("Match between searched for concept '{}' and found concept '{}' is {}", searchedConcept.getName(),
//                    foundConcept.getLabel(), map(scores.getScores(), Score::getScore));
//        }
//    }

//    public static void main(String[] args)
//    {
//        ConceptInsights conceptInsightsService = CiDefaults.getConceptInsightsService();
//
//        Corpus corpus = CiCorpusHelper.createNewCorpus(conceptInsightsService, CiDefaults.getAccountId(), "corpusC");
//        System.out.println("corpus = " + corpus);
//
////        Corpus corpus = CiDefaults.getSelectedCorpus(conceptInsightsService);
//////        CiCorpusHelper.getAccountsInfo(conceptInsightsService);
//////        CiCorpusHelper.getCorpiInfo(conceptInsightsService);
////
//////        CiCorpusQuerier ciCorpusQuerier = CiCorpusQuerier.getInstance(true);
//////        ciCorpusQuerier.testFindDocumentsByTextQuerySingleConcept("Dredging");
////
////        List<Document> documents = CiCorpusHelper.findDocumentsByPartialName(conceptInsightsService, corpus, "boskalis");
//////        List<Document> documents = CiCorpusHelper.findDocumentsByPartialName(conceptInsightsService, corpus, "www_boskalis_com_dredging");
////        documents.forEach(System.out::println);
////
//////        for (Document document : documents)
//////            CiCorpusQuerierOtherQueries.getInstance().showDocumentAnnotations(document);
//
//    }

//    public static Corpus getCorpusByName(ConceptInsights conceptInsightsService, String corpusName)
//    {
//        Corpus tempCorpus;
//        Corpus corpus;
//
//        tempCorpus = new Corpus(CiDefaults.ACCOUNT_ID, corpusName);
//        int retryTime = 5;
//        while (true) {
//            try {
//                corpus = conceptInsightsService.getCorpus(tempCorpus);
//                return corpus;
//            } catch (Exception e) {
//                e.printStackTrace();
//                log.warn("Getting corpus {} failed, retrying in {} seconds...");
//                try {
//                    Thread.sleep(retryTime * 1000);
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
//                if (retryTime == 30) retryTime = 60;
//                if (retryTime == 20) retryTime = 30;
//                if (retryTime == 10) retryTime = 20;
//                if (retryTime == 5) retryTime = 10;
//            }
//        }
//    }
}
