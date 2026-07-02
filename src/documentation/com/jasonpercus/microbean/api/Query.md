# 📄 Query [Type: Annotation]

## 🎯 Description
`@Query` marque un paramètre de méthode comme provenant de la query string HTTP.

L'annotation indique le nom du paramètre à rechercher dans l'URL (exemple: `?name=alice`).

## 🧠 Rôle dans l'architecture MicroBean
- Pourquoi ça existe: mapper directement les paramètres d'URL vers les arguments des méthodes controller.
- Quel problème ça résout: éviter le parsing manuel de la query string dans le code applicatif.
- Où cela s'intègre dans le conteneur IoC: elle est traitée par l'infrastructure REST au moment de résoudre les arguments d'appel.

## 🔗 Relations
- Dépend de:
  - `java.lang.annotation.Target`
  - `java.lang.annotation.Retention`
  - `java.lang.annotation.RetentionPolicy`
  - `java.lang.annotation.ElementType`
- Utilisé par:
  - Le resolveur de paramètres HTTP
  - Les méthodes controller filtrant/recherchant par critères
- Concepts liés:
  - `@Path`, `@Header`, `@Body`
  - `@Get`, `@Post`, `@Put`, `@Delete`, `@Patch`, `@Head`, `@Options`

## 🔧 Méthodes (le cas échéant)
| Method    | Return type | Visibility | Description                                   |
|-----------|-------------|------------|-----------------------------------------------|
| `value()` | `String`    | `public`   | Nom du paramètre à lire dans la query string. |

## 💡 Exemple d'utilisation
```java
import com.jasonpercus.microbean.api.Controller;
import com.jasonpercus.microbean.api.Query;
import com.jasonpercus.microbean.api.method.Get;

@Controller("/users")
public class UserController {

    @Get
    public String find(@Query("name") String name) {
        return name;
    }
}
```

## 🔄 Comportement lié au cycle de vie (le cas échéant)
`@Query` est conservée à l'exécution (`RetentionPolicy.RUNTIME`) et évaluée à chaque requête pour extraire la valeur associée dans la query string.

## ⚠️ Limites / cas particuliers
- L'annotation ne s'applique qu'aux paramètres (`@Target(ElementType.PARAMETER)`).
- Si le paramètre est absent, la valeur finale dépend du type cible et de la stratégie de conversion.
- En cas de multi-valeurs pour une même clé, le comportement dépend du resolveur HTTP interne.

## 📍 Notes internes sur MicroBean
- L'annotation expose uniquement `value()` pour rester explicite et compacte.
- Le décodage URL et la conversion de type sont gérés dans la couche infrastructure REST.
