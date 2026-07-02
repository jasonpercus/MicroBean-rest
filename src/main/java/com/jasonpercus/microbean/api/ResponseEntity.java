package com.jasonpercus.microbean.api;

/*
 * Copyright (c) 2026 JasonPercus
 *
 * Licensed under the MIT License.
 * See LICENSE file in the project root for more information.
 */

/**
 * Classe générique représentant une réponse HTTP avec un code de statut et un corps de réponse.
 * Cette classe utilise une API fluent pour permettre la construction facile de réponses HTTP complètes.
 *
 * @param <R> le type générique du corps de réponse
 */
public class ResponseEntity <R> {

    /**
     * Le corps de la réponse HTTP.
     */
    private R body;

    /**
     * Le code de statut HTTP de la réponse (par défaut 200 OK).
     */
    private int code;

    /**
     * Constructeur par défaut qui initialise le code de réponse HTTP à 200 (OK).
     */
    public ResponseEntity() {
        code = 200;
    }

    /**
     * Récupère le corps de la réponse HTTP.
     *
     * @return le corps de réponse de type R, ou null si non défini.
     */
    public R getBody() {
        return body;
    }

    /**
     * Récupère le code de statut HTTP de la réponse.
     *
     * @return le code de statut HTTP (par défaut 200).
     */
    public int getCode() {
        return code;
    }

    /**
     * Définit le corps de la réponse de manière fluent.
     *
     * @param result le corps de réponse à définir.
     * @return l'instance courante de ResponseEntity pour permettre le chaînage de méthodes.
     */
    public ResponseEntity<R> setBody(R result) {
        this.body = result;

        return this;
    }

    /**
     * Définit le code de statut HTTP de manière fluent.
     * Cette méthode constitue la base commune pour toutes les autres méthodes de statut.
     *
     * @param code le code de statut HTTP à définir.
     * @return l'instance courante de ResponseEntity pour permettre le chaînage de méthodes.
     */
    public ResponseEntity<R> code(int code) {
        this.code = code;

        return this;
    }

    /**
     * Définit le code de réponse HTTP à 100 (CONTINUE).
     * À utiliser quand le client peut poursuivre l'envoi du corps de requête.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 100.
     */
    public ResponseEntity<R> continueResponse() {
        return code(100);
    }

    /**
     * Définit le code de réponse HTTP à 101 (SWITCHING PROTOCOLS).
     * À utiliser pour confirmer un changement de protocole (ex: HTTP vers WebSocket).
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 101.
     */
    public ResponseEntity<R> switchingProtocols() {
        return code(101);
    }

    /**
     * Définit le code de réponse HTTP à 102 (PROCESSING).
     * À utiliser pour signaler un traitement long en cours (WebDAV).
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 102.
     */
    public ResponseEntity<R> processing() {
        return code(102);
    }

    /**
     * Définit le code de réponse HTTP à 103 (EARLY HINTS).
     * À utiliser pour envoyer des indices de préchargement avant la réponse finale.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 103.
     */
    public ResponseEntity<R> earlyHints() {
        return code(103);
    }

    /**
     * Définit le code de réponse HTTP à 200 (OK).
     * À utiliser quand la requête est traitée avec succès et qu'une réponse standard est renvoyée.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 200.
     */
    public ResponseEntity<R> ok() {
        return code(200);
    }

    /**
     * Définit le code de réponse HTTP à 201 (CREATED).
     * À utiliser après la création effective d'une nouvelle ressource.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 201.
     */
    public ResponseEntity<R> created() {
        return code(201);
    }

    /**
     * Définit le code de réponse HTTP à 202 (ACCEPTED).
     * À utiliser quand la requête est acceptée mais traitée de façon asynchrone.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 202.
     */
    public ResponseEntity<R> accepted() {
        return code(202);
    }

    /**
     * Définit le code de réponse HTTP à 203 (NON-AUTHORITATIVE INFORMATION).
     * À utiliser quand les métadonnées renvoyées proviennent d'une source transformée/intermédiaire.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 203.
     */
    public ResponseEntity<R> nonAuthoritativeInformation() {
        return code(203);
    }

    /**
     * Définit le code de réponse HTTP à 204 (NO CONTENT).
     * À utiliser quand l'opération réussit sans corps de réponse à retourner.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 204.
     */
    public ResponseEntity<R> noContent() {
        return code(204);
    }

    /**
     * Définit le code de réponse HTTP à 205 (RESET CONTENT).
     * À utiliser pour demander au client de réinitialiser son état d'interface après succès.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 205.
     */
    public ResponseEntity<R> resetContent() {
        return code(205);
    }

    /**
     * Définit le code de réponse HTTP à 206 (PARTIAL CONTENT).
     * À utiliser lors d'une réponse partielle à une requête Range.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 206.
     */
    public ResponseEntity<R> partialContent() {
        return code(206);
    }

    /**
     * Définit le code de réponse HTTP à 207 (MULTI-STATUS).
     * À utiliser pour retourner plusieurs statuts dans une seule réponse (WebDAV).
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 207.
     */
    public ResponseEntity<R> multiStatus() {
        return code(207);
    }

    /**
     * Définit le code de réponse HTTP à 208 (ALREADY REPORTED).
     * À utiliser en WebDAV pour éviter de répéter des éléments déjà décrits.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 208.
     */
    public ResponseEntity<R> alreadyReported() {
        return code(208);
    }

    /**
     * Définit le code de réponse HTTP à 210 (CONTENT DIFFERENT).
     * Code non standard à utiliser uniquement si votre infrastructure le reconnaît.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 210.
     */
    public ResponseEntity<R> contentDifferent() {
        return code(210);
    }

    /**
     * Définit le code de réponse HTTP à 226 (IM USED).
     * À utiliser pour indiquer qu'une manipulation d'instance a été appliquée à la représentation.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 226.
     */
    public ResponseEntity<R> imUsed() {
        return code(226);
    }

    /**
     * Définit le code de réponse HTTP à 300 (MULTIPLE CHOICES).
     * À utiliser quand plusieurs représentations/cibles valides sont disponibles.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 300.
     */
    public ResponseEntity<R> multipleChoices() {
        return code(300);
    }

    /**
     * Définit le code de réponse HTTP à 301 (MOVED PERMANENTLY).
     * À utiliser pour signaler un déplacement permanent de ressource.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 301.
     */
    public ResponseEntity<R> movedPermanently() {
        return code(301);
    }

    /**
     * Définit le code de réponse HTTP à 302 (FOUND).
     * À utiliser pour rediriger temporairement vers une autre URI.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 302.
     */
    public ResponseEntity<R> found() {
        return code(302);
    }

    /**
     * Définit le code de réponse HTTP à 303 (SEE OTHER).
     * À utiliser après une opération pour rediriger vers une ressource de consultation.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 303.
     */
    public ResponseEntity<R> seeOther() {
        return code(303);
    }

    /**
     * Définit le code de réponse HTTP à 304 (NOT MODIFIED).
     * À utiliser avec les mécanismes de cache conditionnels (If-Modified-Since, ETag).
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 304.
     */
    public ResponseEntity<R> notModified() {
        return code(304);
    }

    /**
     * Définit le code de réponse HTTP à 305 (USE PROXY).
     * Code historique/déprécié à utiliser uniquement pour compatibilité legacy.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 305.
     */
    public ResponseEntity<R> useProxy() {
        return code(305);
    }

    /**
     * Définit le code de réponse HTTP à 307 (TEMPORARY REDIRECT).
     * À utiliser pour une redirection temporaire en conservant la méthode HTTP initiale.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 307.
     */
    public ResponseEntity<R> temporaryRedirect() {
        return code(307);
    }

    /**
     * Définit le code de réponse HTTP à 308 (PERMANENT REDIRECT).
     * À utiliser pour une redirection permanente en conservant la méthode HTTP initiale.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 308.
     */
    public ResponseEntity<R> permanentRedirect() {
        return code(308);
    }

    /**
     * Définit le code de réponse HTTP à 310 (TOO MANY REDIRECTS).
     * Code non standard/obsolète à utiliser uniquement en contexte legacy.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 310.
     */
    public ResponseEntity<R> tooManyRedirects() {
        return code(310);
    }

    /**
     * Définit le code de réponse HTTP à 400 (BAD REQUEST).
     * À utiliser quand la requête est invalide (format, paramètres ou validation basique).
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 400.
     */
    public ResponseEntity<R> badRequest() {
        return code(400);
    }

    /**
     * Définit le code de réponse HTTP à 401 (UNAUTHORIZED).
     * À utiliser quand l'authentification est absente ou invalide.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 401.
     */
    public ResponseEntity<R> unauthorized() {
        return code(401);
    }

    /**
     * Définit le code de réponse HTTP à 402 (PAYMENT REQUIRED).
     * À utiliser pour des API monétisées, lorsque l'accès dépend d'un paiement.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 402.
     */
    public ResponseEntity<R> paymentRequired() {
        return code(402);
    }

    /**
     * Définit le code de réponse HTTP à 403 (FORBIDDEN).
     * À utiliser quand l'utilisateur est authentifié mais n'a pas les droits nécessaires.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 403.
     */
    public ResponseEntity<R> forbidden() {
        return code(403);
    }

    /**
     * Définit le code de réponse HTTP à 404 (NOT FOUND).
     * À utiliser quand la ressource demandée n'existe pas.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 404.
     */
    public ResponseEntity<R> notFound() {
        return code(404);
    }

    /**
     * Définit le code de réponse HTTP à 405 (METHOD NOT ALLOWED).
     * À utiliser quand la ressource existe mais n'accepte pas la méthode HTTP utilisée.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 405.
     */
    public ResponseEntity<R> methodNotAllowed() {
        return code(405);
    }

    /**
     * Définit le code de réponse HTTP à 406 (NOT ACCEPTABLE).
     * À utiliser quand aucune représentation ne correspond à l'en-tête Accept du client.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 406.
     */
    public ResponseEntity<R> notAcceptable() {
        return code(406);
    }

    /**
     * Définit le code de réponse HTTP à 407 (PROXY AUTHENTICATION REQUIRED).
     * À utiliser lorsqu'un proxy intermédiaire exige une authentification.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 407.
     */
    public ResponseEntity<R> proxyAuthenticationRequired() {
        return code(407);
    }

    /**
     * Définit le code de réponse HTTP à 408 (REQUEST TIMEOUT).
     * À utiliser quand le serveur abandonne la requête faute de données dans le délai attendu.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 408.
     */
    public ResponseEntity<R> requestTimeout() {
        return code(408);
    }

    /**
     * Définit le code de réponse HTTP à 409 (CONFLICT).
     * À utiliser en cas de conflit d'état (version, verrouillage métier, duplication).
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 409.
     */
    public ResponseEntity<R> conflict() {
        return code(409);
    }

    /**
     * Définit le code de réponse HTTP à 410 (GONE).
     * À utiliser quand une ressource a été retirée de manière définitive.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 410.
     */
    public ResponseEntity<R> gone() {
        return code(410);
    }

    /**
     * Définit le code de réponse HTTP à 411 (LENGTH REQUIRED).
     * À utiliser quand la requête doit fournir un Content-Length valide.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 411.
     */
    public ResponseEntity<R> lengthRequired() {
        return code(411);
    }

    /**
     * Définit le code de réponse HTTP à 412 (PRECONDITION FAILED).
     * À utiliser quand une précondition If-* n'est pas satisfaite.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 412.
     */
    public ResponseEntity<R> preconditionFailed() {
        return code(412);
    }

    /**
     * Définit le code de réponse HTTP à 413 (PAYLOAD TOO LARGE).
     * À utiliser quand le corps de la requête dépasse la taille acceptée.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 413.
     */
    public ResponseEntity<R> payloadTooLarge() {
        return code(413);
    }

    /**
     * Définit le code de réponse HTTP à 414 (URI TOO LONG).
     * À utiliser quand l'URI transmise par le client est trop longue pour être traitée.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 414.
     */
    public ResponseEntity<R> uriTooLong() {
        return code(414);
    }

    /**
     * Définit le code de réponse HTTP à 415 (UNSUPPORTED MEDIA TYPE).
     * À utiliser quand le Content-Type de la requête n'est pas pris en charge.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 415.
     */
    public ResponseEntity<R> unsupportedMediaType() {
        return code(415);
    }

    /**
     * Définit le code de réponse HTTP à 416 (RANGE NOT SATISFIABLE).
     * À utiliser quand la plage demandée est invalide pour la ressource cible.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 416.
     */
    public ResponseEntity<R> rangeNotSatisfiable() {
        return code(416);
    }

    /**
     * Définit le code de réponse HTTP à 417 (EXPECTATION FAILED).
     * À utiliser quand le serveur ne peut pas satisfaire l'en-tête Expect.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 417.
     */
    public ResponseEntity<R> expectationFailed() {
        return code(417);
    }

    /**
     * Définit le code de réponse HTTP à 418 (I'M A TEAPOT).
     * À utiliser uniquement pour des usages spéciaux, de test ou pédagogiques.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 418.
     */
    public ResponseEntity<R> iAmATeapot() {
        return code(418);
    }

    /**
     * Définit le code de réponse HTTP à 419 (AUTHENTICATION TIMEOUT).
     * Code non standard, utile dans certains frameworks quand la session d'authentification expire.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 419.
     */
    public ResponseEntity<R> authenticationTimeout() {
        return code(419);
    }

    /**
     * Définit le code de réponse HTTP à 421 (MISDIRECTED REQUEST).
     * À utiliser quand la requête est adressée à un serveur qui ne peut pas la traiter.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 421.
     */
    public ResponseEntity<R> misdirectedRequest() {
        return code(421);
    }

    /**
     * Définit le code de réponse HTTP à 422 (UNPROCESSABLE ENTITY).
     * À utiliser quand la syntaxe est valide mais que les règles métier/sémantiques échouent.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 422.
     */
    public ResponseEntity<R> unprocessableEntity() {
        return code(422);
    }

    /**
     * Définit le code de réponse HTTP à 423 (LOCKED).
     * À utiliser quand la ressource est verrouillée et ne peut pas être modifiée.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 423.
     */
    public ResponseEntity<R> locked() {
        return code(423);
    }

    /**
     * Définit le code de réponse HTTP à 424 (FAILED DEPENDENCY).
     * À utiliser quand l'opération courante dépend d'une action précédente en échec.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 424.
     */
    public ResponseEntity<R> failedDependency() {
        return code(424);
    }

    /**
     * Définit le code de réponse HTTP à 425 (TOO EARLY).
     * À utiliser pour refuser un rejeu précoce d'une requête non idempotente.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 425.
     */
    public ResponseEntity<R> tooEarly() {
        return code(425);
    }

    /**
     * Définit le code de réponse HTTP à 426 (UPGRADE REQUIRED).
     * À utiliser quand le client doit négocier un protocole plus récent.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 426.
     */
    public ResponseEntity<R> upgradeRequired() {
        return code(426);
    }

    /**
     * Définit le code de réponse HTTP à 427 (SOAP ACTION REQUIRED).
     * Code non standard à utiliser uniquement pour des intégrations SOAP héritées.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 427.
     */
    public ResponseEntity<R> soapActionRequired() {
        return code(427);
    }

    /**
     * Définit le code de réponse HTTP à 428 (PRECONDITION REQUIRED).
     * À utiliser quand le serveur impose une requête conditionnelle pour éviter les conflits d'écriture.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 428.
     */
    public ResponseEntity<R> preconditionRequired() {
        return code(428);
    }

    /**
     * Définit le code de réponse HTTP à 429 (TOO MANY REQUESTS).
     * À utiliser pour indiquer un dépassement de quota ou de limite de débit.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 429.
     */
    public ResponseEntity<R> tooManyRequests() {
        return code(429);
    }

    /**
     * Définit le code de réponse HTTP à 431 (REQUEST HEADER FIELDS TOO LARGE).
     * À utiliser quand les en-têtes de la requête dépassent la taille autorisée.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 431.
     */
    public ResponseEntity<R> requestHeaderFieldsTooLarge() {
        return code(431);
    }

    /**
     * Définit le code de réponse HTTP à 444 (NO RESPONSE).
     * Code propriétaire Nginx indiquant une fermeture de connexion sans réponse HTTP.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 444.
     */
    public ResponseEntity<R> noResponse() {
        return code(444);
    }

    /**
     * Définit le code de réponse HTTP à 449 (RETRY WITH).
     * Code propriétaire Microsoft pour demander une nouvelle tentative après action client.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 449.
     */
    public ResponseEntity<R> retryWith() {
        return code(449);
    }

    /**
     * Définit le code de réponse HTTP à 450 (BLOCKED BY WINDOWS PARENTAL CONTROLS).
     * Code propriétaire Microsoft pour un blocage de politique parentale.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 450.
     */
    public ResponseEntity<R> blockedByWindowsParentalControls() {
        return code(450);
    }

    /**
     * Définit le code de réponse HTTP à 451 (UNAVAILABLE FOR LEGAL REASONS).
     * À utiliser quand une ressource est bloquée pour des raisons juridiques.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 451.
     */
    public ResponseEntity<R> unavailableForLegalReasons() {
        return code(451);
    }

    /**
     * Définit le code de réponse HTTP à 456 (UNRECOVERABLE ERROR).
     * Code non standard à réserver à des conventions internes de plateforme.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 456.
     */
    public ResponseEntity<R> unrecoverableError() {
        return code(456);
    }

    /**
     * Définit le code de réponse HTTP à 495 (SSL CERTIFICATE ERROR).
     * Code propriétaire Nginx pour indiquer une erreur de certificat TLS client.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 495.
     */
    public ResponseEntity<R> sslCertificateError() {
        return code(495);
    }

    /**
     * Définit le code de réponse HTTP à 496 (SSL CERTIFICATE REQUIRED).
     * Code propriétaire Nginx quand un certificat client est requis mais absent.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 496.
     */
    public ResponseEntity<R> sslCertificateRequired() {
        return code(496);
    }

    /**
     * Définit le code de réponse HTTP à 497 (HTTP REQUEST SENT TO HTTPS PORT).
     * Code propriétaire Nginx quand une requête HTTP arrive sur un port réservé à HTTPS.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 497.
     */
    public ResponseEntity<R> httpRequestSentToHttpsPort() {
        return code(497);
    }

    /**
     * Définit le code de réponse HTTP à 498 (INVALID TOKEN).
     * Code non standard utilisé par certains services quand un jeton d'accès est invalide ou expiré.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 498.
     */
    public ResponseEntity<R> invalidToken() {
        return code(498);
    }

    /**
     * Définit le code de réponse HTTP à 499 (CLIENT CLOSED REQUEST).
     * Code propriétaire Nginx indiquant que le client a fermé la connexion avant réponse.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 499.
     */
    public ResponseEntity<R> clientClosedRequest() {
        return code(499);
    }

    /**
     * Définit le code de réponse HTTP à 500 (INTERNAL SERVER ERROR).
     * À utiliser quand une erreur inattendue se produit côté serveur.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 500.
     */
    public ResponseEntity<R> internalServerError() {
        return code(500);
    }

    /**
     * Définit le code de réponse HTTP à 501 (NOT IMPLEMENTED).
     * À utiliser quand la fonctionnalité demandée n'est pas supportée par le serveur.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 501.
     */
    public ResponseEntity<R> notImplemented() {
        return code(501);
    }

    /**
     * Définit le code de réponse HTTP à 502 (BAD GATEWAY).
     * À utiliser en frontal/proxy quand le serveur amont renvoie une réponse invalide.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 502.
     */
    public ResponseEntity<R> badGateway() {
        return code(502);
    }

    /**
     * Définit le code de réponse HTTP à 503 (SERVICE UNAVAILABLE).
     * À utiliser pour une indisponibilité temporaire (maintenance, surcharge, dépendance indisponible).
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 503.
     */
    public ResponseEntity<R> serviceUnavailable() {
        return code(503);
    }

    /**
     * Définit le code de réponse HTTP à 504 (GATEWAY TIMEOUT).
     * À utiliser quand un serveur amont ne répond pas dans le délai imparti.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 504.
     */
    public ResponseEntity<R> gatewayTimeout() {
        return code(504);
    }

    /**
     * Définit le code de réponse HTTP à 505 (HTTP VERSION NOT SUPPORTED).
     * À utiliser quand la version HTTP du client n'est pas prise en charge.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 505.
     */
    public ResponseEntity<R> httpVersionNotSupported() {
        return code(505);
    }

    /**
     * Définit le code de réponse HTTP à 506 (VARIANT ALSO NEGOTIATES).
     * À utiliser en cas d'erreur de négociation de contenu circulaire.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 506.
     */
    public ResponseEntity<R> variantAlsoNegotiates() {
        return code(506);
    }

    /**
     * Définit le code de réponse HTTP à 507 (INSUFFICIENT STORAGE).
     * À utiliser quand le serveur ne dispose pas des ressources de stockage nécessaires.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 507.
     */
    public ResponseEntity<R> insufficientStorage() {
        return code(507);
    }

    /**
     * Définit le code de réponse HTTP à 508 (LOOP DETECTED).
     * À utiliser lorsqu'une boucle de traitement est détectée (notamment en WebDAV).
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 508.
     */
    public ResponseEntity<R> loopDetected() {
        return code(508);
    }

    /**
     * Définit le code de réponse HTTP à 509 (BANDWIDTH LIMIT EXCEEDED).
     * Code non standard pour signaler un dépassement de bande passante allouée.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 509.
     */
    public ResponseEntity<R> bandwidthLimitExceeded() {
        return code(509);
    }

    /**
     * Définit le code de réponse HTTP à 510 (NOT EXTENDED).
     * À utiliser quand des extensions de requête sont nécessaires mais non fournies.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 510.
     */
    public ResponseEntity<R> notExtended() {
        return code(510);
    }

    /**
     * Définit le code de réponse HTTP à 511 (NETWORK AUTHENTICATION REQUIRED).
     * À utiliser pour demander une authentification réseau (ex: portail captif).
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 511.
     */
    public ResponseEntity<R> networkAuthenticationRequired() {
        return code(511);
    }

    /**
     * Définit le code de réponse HTTP à 520 (WEB SERVER RETURNED AN UNKNOWN ERROR).
     * Code propriétaire Cloudflare indiquant une erreur d'origine non catégorisée.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 520.
     */
    public ResponseEntity<R> webServerReturnedUnknownError() {
        return code(520);
    }

    /**
     * Définit le code de réponse HTTP à 521 (WEB SERVER IS DOWN).
     * Code propriétaire Cloudflare quand le serveur d'origine est indisponible.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 521.
     */
    public ResponseEntity<R> webServerIsDown() {
        return code(521);
    }

    /**
     * Définit le code de réponse HTTP à 522 (CONNECTION TIMED OUT).
     * Code propriétaire Cloudflare quand la connexion vers l'origine expire.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 522.
     */
    public ResponseEntity<R> connectionTimedOut() {
        return code(522);
    }

    /**
     * Définit le code de réponse HTTP à 523 (ORIGIN IS UNREACHABLE).
     * Code propriétaire Cloudflare quand l'origine ne peut pas être jointe.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 523.
     */
    public ResponseEntity<R> originIsUnreachable() {
        return code(523);
    }

    /**
     * Définit le code de réponse HTTP à 524 (A TIMEOUT OCCURRED).
     * Code propriétaire Cloudflare quand l'origine met trop de temps à répondre.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 524.
     */
    public ResponseEntity<R> aTimeoutOccurred() {
        return code(524);
    }

    /**
     * Définit le code de réponse HTTP à 525 (SSL HANDSHAKE FAILED).
     * Code propriétaire Cloudflare quand la négociation TLS avec l'origine échoue.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 525.
     */
    public ResponseEntity<R> sslHandshakeFailed() {
        return code(525);
    }

    /**
     * Définit le code de réponse HTTP à 526 (INVALID SSL CERTIFICATE).
     * Code propriétaire Cloudflare quand le certificat SSL d'origine est invalide.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 526.
     */
    public ResponseEntity<R> invalidSslCertificate() {
        return code(526);
    }

    /**
     * Définit le code de réponse HTTP à 527 (RAILGUN ERROR).
     * Code propriétaire Cloudflare pour des erreurs liées au composant Railgun.
     *
     * @return l'instance de ResponseEntity avec le code de réponse défini à 527.
     */
    public ResponseEntity<R> railgunError() {
        return code(527);
    }
}
