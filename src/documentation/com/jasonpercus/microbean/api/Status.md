# 📄 Status [Type: Annotation]

## 🎯 Description
`@Status` permet de fixer explicitement le code de statut HTTP renvoyé par une méthode controller.

Elle s'applique sur une méthode pour surcharger le statut standard utilisé par défaut par l'infrastructure REST.

## 🧠 Rôle dans l'architecture MicroBean
- Pourquoi ça existe: contrôler précisément la sémantique HTTP d'une réponse.
- Quel problème ça résout: éviter un statut générique unique quand un cas métier nécessite un code spécifique.
- Où cela s'intègre dans le conteneur IoC: elle est lue au runtime pendant l'écriture de la réponse HTTP d'une méthode controller.

## 🔗 Relations
- Dépend de:
  - `java.lang.annotation.Target`
  - `java.lang.annotation.Retention`
  - `java.lang.annotation.RetentionPolicy`
  - `java.lang.annotation.ElementType`
- Utilisé par:
  - Le writer de réponse HTTP de l'infrastructure REST
  - Les méthodes controller qui doivent retourner un statut précis
- Concepts liés:
  - `@Post`, `@Put`, `@Patch`, `@Delete`, `@Get`, `@Head`, `@Options`
  - Gestion des réponses HTTP

## 🔧 Méthodes (le cas échéant)
| Method    | Return type | Visibility | Description                                             |
|-----------|-------------|------------|---------------------------------------------------------|
| `value()` | `int`       | `public`   | Code HTTP à retourner (ex: `200`, `201`, `204`, `404`). |

## 💡 Exemple d'utilisation
```java
import com.jasonpercus.microbean.api.Body;
import com.jasonpercus.microbean.api.Controller;
import com.jasonpercus.microbean.api.Status;
import com.jasonpercus.microbean.api.method.Post;

@Controller("/users")
public class UserController {

    @Post
    @Status(201)
    public User create(@Body User user) {
        return user;
    }
}
```

## 🔄 Comportement lié au cycle de vie (le cas échéant)
`@Status` est conservée à l'exécution (`RetentionPolicy.RUNTIME`) et utilisée au moment de la construction de la réponse HTTP pour déterminer le code à envoyer.

## ⚠️ Limites / cas particuliers
- L'annotation ne s'applique qu'aux méthodes (`@Target(ElementType.METHOD)`).
- Elle ne valide pas la cohérence métier du code HTTP choisi ; cette responsabilité reste applicative.
- En cas de logique interne imposant un autre statut (ex: gestion d'erreur), ce statut peut primer selon l'implémentation serveur.

## 📍 Notes internes sur MicroBean
- `@Status` est volontairement compacte avec une seule propriété `value()`.
- La décision finale d'écriture de réponse reste centralisée dans l'infrastructure REST.
