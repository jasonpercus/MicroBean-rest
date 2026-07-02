# 📄 Path [Type: Annotation]

## 🎯 Description
`@Path` marque un paramètre de méthode comme provenant d'une variable de chemin URL.

L'annotation lie un nom de variable de route (exemple: `/users/{id}`) à un argument Java.

## 🧠 Rôle dans l'architecture MicroBean
- Pourquoi ça existe: relier proprement les segments dynamiques de route aux paramètres des handlers.
- Quel problème ça résout: éviter l'extraction et la conversion manuelles des segments d'URL.
- Où cela s'intègre dans le conteneur IoC: elle est interprétée par l'infrastructure REST au moment du binding des paramètres de méthode.

## 🔗 Relations
- Dépend de:
  - `java.lang.annotation.Target`
  - `java.lang.annotation.Retention`
  - `java.lang.annotation.RetentionPolicy`
  - `java.lang.annotation.ElementType`
- Utilisé par:
  - Le moteur de résolution des paramètres de route
  - Les méthodes controller manipulant des identifiants de ressource
- Concepts liés:
  - `@Controller`
  - `@Get`, `@Put`, `@Patch`, `@Delete`, `@Head`, `@Options`
  - `@Query`, `@Header`, `@Body`

## 🔧 Méthodes (le cas échéant)
| Method    | Return type | Visibility | Description                                           |
|-----------|-------------|------------|-------------------------------------------------------|
| `value()` | `String`    | `public`   | Nom de la variable de chemin à extraire depuis l'URL. |

## 💡 Exemple d'utilisation
```java
import com.jasonpercus.microbean.api.Controller;
import com.jasonpercus.microbean.api.Path;
import com.jasonpercus.microbean.api.method.Get;

@Controller("/users")
public class UserController {

    @Get("/{id}")
    public long id(@Path("id") long id) {
        return id;
    }
}
```

## 🔄 Comportement lié au cycle de vie (le cas échéant)
`@Path` est conservée à l'exécution (`RetentionPolicy.RUNTIME`) et utilisée lors du matching de route pour injecter les segments dynamiques dans les arguments de méthode.

## ⚠️ Limites / cas particuliers
- L'annotation ne s'applique qu'aux paramètres (`@Target(ElementType.PARAMETER)`).
- Le nom fourni doit correspondre à un placeholder existant dans le chemin de route.
- Une valeur non convertible vers le type cible peut provoquer une erreur de résolution.

## 📍 Notes internes sur MicroBean
- `@Path` reste volontairement simple avec une seule propriété `value()`.
- La logique de matching de route et de conversion des types est portée par l'infrastructure REST.
