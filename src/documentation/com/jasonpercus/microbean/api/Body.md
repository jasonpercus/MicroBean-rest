# 📄 Body [Type: Annotation]

## 🎯 Description
`@Body` marque un paramètre de méthode comme source de données issue du corps de la requête HTTP.

Elle est utilisée pour déclencher la désérialisation du payload (souvent JSON) vers le type Java du paramètre.

## 🧠 Rôle dans l'architecture MicroBean
- Pourquoi ça existe: indiquer explicitement qu'un argument doit être résolu depuis le corps HTTP.
- Quel problème ça résout: éviter l'ambiguïté entre paramètres de chemin, query, headers et body.
- Où cela s'intègre dans le conteneur IoC: elle est exploitée au runtime par la couche REST lors de la construction des arguments d'invocation.

## 🔗 Relations
- Dépend de:
  - `java.lang.annotation.Target`
  - `java.lang.annotation.Retention`
  - `java.lang.annotation.RetentionPolicy`
  - `java.lang.annotation.ElementType`
- Utilisé par:
  - Le resolveur d'arguments de l'infrastructure REST
  - Les méthodes controller recevant des objets JSON
- Concepts liés:
  - `@Post`, `@Put`, `@Patch`
  - `@Path`, `@Query`, `@Header`

## 🔧 Méthodes (le cas échéant)
Non applicable.

## 💡 Exemple d'utilisation
```java
import com.jasonpercus.microbean.api.Body;
import com.jasonpercus.microbean.api.Controller;
import com.jasonpercus.microbean.api.method.Post;

@Controller("/users")
public class UserController {

    @Post
    public User create(@Body User user) {
        return user;
    }
}
```

## 🔄 Comportement lié au cycle de vie (le cas échéant)
`@Body` est conservée à l'exécution (`RetentionPolicy.RUNTIME`) et interprétée à chaque requête pour convertir le corps HTTP vers le type du paramètre ciblé.

## ⚠️ Limites / cas particuliers
- L'annotation ne s'applique qu'aux paramètres (`@Target(ElementType.PARAMETER)`).
- Si le corps est absent ou invalide, la résolution dépend de la stratégie d'erreur de l'infrastructure REST.
- Le format réellement supporté dépend du parseur configuré côté serveur (JSON dans le comportement actuel).

## 📍 Notes internes sur MicroBean
- `@Body` ne porte aucune propriété: son rôle est uniquement déclaratif.
- La conversion et la validation sont déléguées à la couche serveur, pas à l'annotation.
