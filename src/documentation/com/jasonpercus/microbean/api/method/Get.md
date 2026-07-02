# 📄 Get [Type: Annotation]

## 🎯 Description
`@Get` déclare qu'une méthode de controller traite une requête HTTP de type `GET`.

Cette annotation permet d'associer un sous-chemin optionnel à la méthode, qui sera combiné avec le chemin de base du controller.

## 🧠 Rôle dans l'architecture MicroBean
- Pourquoi ça existe: déclarer explicitement les handlers HTTP `GET` dans les controllers.
- Quel problème ça résoud: éviter la configuration manuelle des routes et centraliser le mapping dans les annotations.
- Où cela s'intègre dans le conteneur IoC: elle est lue au runtime pendant le scan des beans `@Controller` afin de construire la table de routage REST.

## 🔗 Relations
- Dépend de:
  - `java.lang.annotation.Target`
  - `java.lang.annotation.Retention`
  - `java.lang.annotation.RetentionPolicy`
  - `java.lang.annotation.ElementType`
- Utilisé par:
  - Le service d'exposition REST lors de l'analyse des méthodes de controller
  - Les classes de controller applicatives
- Concepts liés:
  - `@Controller`
  - `@Post`, `@Put`, `@Delete`, `@Patch`, `@Head`, `@Options`
  - Résolution de routes HTTP

## 🔧 Méthodes (le cas échéant)
| Method    | Return type | Visibility | Description                                                                        |
|-----------|-------------|------------|------------------------------------------------------------------------------------|
| `value()` | `String`    | `public`   | Sous-chemin de la route `GET`, relatif au chemin `@Controller`. Par défaut : `""`. |

## 💡 Exemple d'utilisation
```java
import com.jasonpercus.microbean.api.Controller;
import com.jasonpercus.microbean.api.method.Get;

@Controller("/users")
public class UserController {

	@Get
	public String root() {
		return "users root";
	}

	@Get("/{id}")
	public String findById() {
		return "one user";
	}
}
```

## 🔄 Comportement lié au cycle de vie (le cas échéant)
`@Get` est conservée à l'exécution (`RetentionPolicy.RUNTIME`) et interprétée pendant la phase de scan des controllers pour créer les définitions de routes HTTP.

## ⚠️ Limites / cas particuliers
- L'annotation ne s'applique qu'aux méthodes (`@Target(ElementType.METHOD)`).
- Si `value()` est vide, la route correspond au chemin de base du controller.
- Le mapping final dépend de la concaténation chemin controller + `value()`.
- L'annotation ne gère pas elle-même la validation des paramètres, la sérialisation JSON ou les statuts HTTP.

## 📍 Notes internes sur MicroBean
- L'annotation est minimale par design : une seule propriété `value()` pour garder un mapping explicite et lisible.
- Le traitement fonctionnel (résolution d'arguments, conversion de types, écriture de réponse) est délégué à l'infrastructure REST, pas à l'annotation.
