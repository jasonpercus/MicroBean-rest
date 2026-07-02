# 📄 Header [Type: Annotation]

## 🎯 Description
`@Header` marque un paramètre de méthode comme provenant d'un header HTTP.

L'annotation identifie le nom du header à lire dans la requête entrante.

## 🧠 Rôle dans l'architecture MicroBean
- Pourquoi ça existe: mapper explicitement des métadonnées HTTP vers des paramètres Java.
- Quel problème ça résout: éviter l'accès manuel aux headers dans chaque méthode controller.
- Où cela s'intègre dans le conteneur IoC: elle est interprétée par la couche REST au moment de la résolution des arguments.

## 🔗 Relations
- Dépend de:
  - `java.lang.annotation.Target`
  - `java.lang.annotation.Retention`
  - `java.lang.annotation.RetentionPolicy`
  - `java.lang.annotation.ElementType`
- Utilisé par:
  - Le resolveur de paramètres HTTP
  - Les méthodes controller nécessitant des headers (authentification, tracing, etc.)
- Concepts liés:
  - `@Body`, `@Query`, `@Path`
  - `@Get`, `@Post`, `@Put`, `@Delete`, `@Patch`, `@Head`, `@Options`

## 🔧 Méthodes (le cas échéant)
| Method    | Return type | Visibility | Description                                  |
|-----------|-------------|------------|----------------------------------------------|
| `value()` | `String`    | `public`   | Nom du header HTTP à extraire de la requête. |

## 💡 Exemple d'utilisation
```java
import com.jasonpercus.microbean.api.Controller;
import com.jasonpercus.microbean.api.Header;
import com.jasonpercus.microbean.api.method.Get;

@Controller("/secure")
public class SecureController {

    @Get
    public String info(@Header("Authorization") String token) {
        return token;
    }
}
```

## 🔄 Comportement lié au cycle de vie (le cas échéant)
`@Header` est conservée à l'exécution (`RetentionPolicy.RUNTIME`) et utilisée à chaque requête pour lire la valeur du header ciblé.

## ⚠️ Limites / cas particuliers
- L'annotation ne s'applique qu'aux paramètres (`@Target(ElementType.PARAMETER)`).
- Si le header n'est pas présent, la valeur reçue dépend du type du paramètre et de la stratégie de conversion/erreur.
- La casse du nom de header peut dépendre de l'implémentation HTTP sous-jacente.

## 📍 Notes internes sur MicroBean
- L'annotation reste volontairement minimaliste avec un seul champ `value()`.
- Le mécanisme de conversion du type cible est géré en dehors de l'annotation.
