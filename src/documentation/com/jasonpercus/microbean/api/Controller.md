# 📄 Controller [Type: Annotation]

## 🎯 Description
`@Controller` marque une classe comme composant REST MicroBean.

Elle combine un rôle de bean IoC (via `@Service`) et un rôle d'exposition HTTP en définissant un chemin de base utilisé par les méthodes annotées (`@Get`, `@Post`, etc.).

## 🧠 Rôle dans l'architecture MicroBean
- Pourquoi ça existe: identifier explicitement les classes qui portent des routes HTTP.
- Quel problème ça résout: regrouper les handlers REST dans des composants scannables et configurables.
- Où cela s'intègre dans le conteneur IoC: l'annotation est détectée pendant le scan des beans puis exploitée par l'infrastructure REST pour construire les routes.

## 🔗 Relations
- Dépend de:
  - `@Service`
  - `java.lang.annotation.Target`
  - `java.lang.annotation.Retention`
  - `java.lang.annotation.RetentionPolicy`
  - `java.lang.annotation.ElementType`
- Utilisé par:
  - Le scanner de beans MicroBean
  - Le moteur de routage REST
- Concepts liés:
  - `@Get`, `@Post`, `@Put`, `@Delete`, `@Patch`, `@Head`, `@Options`
  - `@Path`, `@Query`, `@Header`, `@Body`

## 🔧 Méthodes (le cas échéant)
| Method    | Return type | Visibility | Description                                         |
|-----------|-------------|------------|-----------------------------------------------------|
| `name()`  | `String`    | `public`   | Nom explicite du bean controller. Par défaut: `""`. |
| `value()` | `String`    | `public`   | Chemin de base du controller. Par défaut: `""`.     |

## 💡 Exemple d'utilisation
```java
import com.jasonpercus.microbean.api.Controller;
import com.jasonpercus.microbean.api.method.Get;

@Controller("/users")
public class UserController {

    @Get("/all")
    public String all() {
        return "ok";
    }
}
```

## 🔄 Comportement lié au cycle de vie (le cas échéant)
`@Controller` est conservée à l'exécution (`RetentionPolicy.RUNTIME`) et traitée pendant le scan IoC pour enregistrer la classe comme bean et point d'entrée REST.

## ⚠️ Limites / cas particuliers
- L'annotation ne s'applique qu'aux classes (`@Target(ElementType.TYPE)`).
- Le chemin final d'une route dépend de la concaténation `@Controller.value()` + annotation HTTP de méthode.
- `name()` et `value()` vides laissent les comportements par défaut au conteneur et au routeur.

## 📍 Notes internes sur MicroBean
- `@Controller` est méta-annotée avec `@Service`, ce qui évite de dupliquer les annotations sur les classes REST.
- Le mapping HTTP réel est géré par l'infrastructure REST, pas par l'annotation elle-même.
