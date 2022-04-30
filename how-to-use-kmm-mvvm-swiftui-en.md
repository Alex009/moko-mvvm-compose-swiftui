# How to use Kotlin Multiplatform ViewModel in SwiftUI and Jetpack Compose

We at [IceRock Development](https://icerock.dev) have been using the MVVM approach for many years, and the last
4 years our `ViewModel` are shared in the common code. We do it by using our library
[moko-mvvm](https://github.com/icerockdev/moko-mvvm). In the last year, we have been actively moving to
using Jetpack Compose and SwiftUI to build UI in our projects. And it require
MOKO MVVM improvements to make it more comfortable for developers on both platforms to work with this approach.

On April 30, 2022, [new version of MOKO MVVM - 0.13.0](https://github.com/icerockdev/moko-mvvm/releases/tag/release%2F0.13.0) was released.
This version has full support for Jetpack Compose and SwiftUI. Let's take an example of how
you can use ViewModel from common code with these frameworks.

The example will be simple - an application with an authorization screen. Two input fields - login and password, button
Log in and a message about a successful login after a second of waiting (while waiting, we turn the progress bar).

## Create a project

The first step is simple - take Android Studio, install
[Kotlin Multiplatform Mobile IDE plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile),
if not already installed. Create a project according to the template "Kotlin Multiplatform App" using
`CocoaPods integration` (it’s more convenient with them, plus we need it to connect an additional CocoaPod later).

![wizard-cocoapods](media/wizard-cocoapods-integration.png)

[git commit](https://github.com/Alex009/moko-mvvm-compose-swiftui/commit/ee223a80e17616e622d135c0651ab454eabfad7a)

## Login screen on Android with Jetpack Compose

The app template uses the standard Android View approach, so we need to enable
Jetpack Compose before implementation of UI.

Enable Compose support in `androidApp/build.gradle.kts`:
```kotlin
val composeVersion = "1.1.1"

android {
    // ...

    buildFeatures {
        compose=true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = composeVersion
    }
}
```

And we add the dependencies we need, removing the old unnecessary ones (related to the usual approach with view):
```kotlin
dependencies {
    implementation(project(":shared"))

    implementation("androidx.compose.foundation:foundation:$composeVersion")
    implementation("androidx.compose.runtime:runtime:$composeVersion")
    // UI
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling:$composeVersion")
    // material design
    implementation("androidx.compose.material:material:$composeVersion")
    implementation("androidx.compose.material:material-icons-core:$composeVersion")
    // Activity
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
}
```

When running Gradle Sync, we get a message about the version incompatibility between Jetpack Compose and Kotlin.
This is due to the fact that Compose uses a compiler plugin for Kotlin, and the compiler plugin APIs are not yet stabilized. Therefore, we need to install the version of Kotlin that supports
the Compose version we are using is `1.6.10`.

Next, it remains to implement the authorization screen, I immediately give the finished code:
```kotlin
@Composable
fun LoginScreen() {
    val context: Context = LocalContext.current
    val coroutineScope: CoroutineScope = rememberCoroutineScope()

    var login: String by remember { mutableStateOf("") }
    var password: String by remember { mutableStateOf("") }
    var isLoading: Boolean by remember { mutableStateOf(false) }

    val isLoginButtonEnabled: Boolean = login.isNotBlank() && password.isNotBlank() && !isLoading

    Column(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = login,
            enabled = !isLoading,
            label = { Text(text = "Login") },
            onValueChange = { login = it }
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            enabled = !isLoading,
            label = { Text(text = "Password") },
            visualTransformation = PasswordVisualTransformation(),
            onValueChange = { password = it }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = isLoginButtonEnabled,
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    delay(1000)
                    isLoading = false
                    Toast.makeText(context, "login success!", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size
(24.dp))
            else Text(text = "login")
        }
    }
}
```

And here is our android app with authorization screen ready and functioning as required, but without
common code.

![android-compose-mvvm](media/android-compose-mvvm.gif)

[git commit](https://github.com/Alex009/moko-mvvm-compose-swiftui/commit/69cf1904cd16f34b5bc646cdcacda3b72c8b58cf)

## Authorization screen in iOS with SwiftUI

Let's make the same screen in SwiftUI. The template has already created a SwiftUI app, so it's easy enough for us to
write screen code. We get the following code:

```swift
struct LoginScreen: View {
    @State private var login: String = ""
    @State private var password: String = ""
    @State private var isLoading: Bool = false
    @State private var isSuccessfulAlertShowed: Bool = false
    
    private var isButtonEnabled: Bool {
        get {
            !isLoading && !login.isEmpty && !password.isEmpty
        }
    }
    
    var body: someView {
        Group {
            VStack(spacing: 8.0) {
                TextField("Login", text: $login)
                    .textFieldStyle(.roundedBorder)
                    .disabled(isLoading)
                
                SecureField("Password", text: $password)
                    .textFieldStyle(.roundedBorder)
                    .disabled(isLoading)
                
                Button(
                    action: {
                        isLoading = true
                        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
                            isLoading = false
                            isSuccessfulAlertShowed = true
                        }
                    }, label: {
                        if isLoading {
                            ProgressView()
                        } else {
                            Text("login")
                        }
                    }
                ).disabled(!isButtonEnabled)
            }.padding()
        }.alert(
            "Login successful",
            isPresented: $isSuccessfulAlertShowed
        ) {
            Button("Close", action: { isSuccessfulAlertShowed = false })
        }
    }
}
```

The logic of work is completely identical to the Android version and also does not use any common logic.

![ios-swiftui-mvvm](media/ios-swiftui-mvvm.gif)

[git commit](https://github.com/Alex009/moko-mvvm-compose-swiftui/commit/760622ab392b1e723e4bb508d8f5c8b97b9ca5a7)

## Implement a common ViewModel

All preparatory steps are completed. It's time to move the authorization screen logic out of the platforms in
common code.

The first thing we will do for this is to connect the moko-mvvm dependency to the common module and add it to
export list for iOS framework (so that in Swift we can see all public classes and methods of this
libraries).

```kotlin
val mokoMvvmVersion = "0.13.0"

kotlin {
    // ...

    cocoapods {
        // ...
        
        framework {
            baseName = "MultiPlatformLibrary"

            export("dev.icerock.moko:mvvm-core:$mokoMvvmVersion")
            export("dev.icerock.moko:mvvm-flow:$mokoMvvmVersion")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1-native-mt")

                api("dev.icerock.moko:mvvm-core:$mokoMvvmVersion")
                api("dev.icerock.moko:mvvm-flow:$mokoMvvmVersion")
            }
        }
        // ...
        val androidMain by getting {
            dependencies {
                api("dev.icerock.moko:mvvm-flow-compose:$mokoMvvmVersion")
            }
        }
        // ...
    }
}
```

We also changed the `baseName` of the iOS Framework to `MultiPlatformLibrary`. This is an important change,
which we will not be able to connect CocoaPod with Kotlin and SwiftUI integration functions in the future.

It remains to write the `LoginViewModel` itself. Here is the code:
```kotlin
class LoginViewModel : ViewModel() {
    val login: MutableStateFlow<String> = MutableStateFlow("")
    val password: MutableStateFlow<String> = MutableStateFlow("")

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    val isButtonEnabled: StateFlow<Boolean> =
        combine(isLoading, login, password) { isLoading, login, password ->
            isLoading.not() && login.isNotBlank() && password.isNotBlank()
        }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _actions = Channel<Action>()
    val actions: Flow<Action> get() = _actions.receiveAsFlow()

    fun onLoginPressed() {
        _isLoading.value = true
        viewModelScope.launch {
            delay(1000)
            _isLoading.value = false
            _actions.send(Action.LoginSuccess)
        }
    }

    sealed interface Action {
        object LoginSuccess : Action
    }
}
```

For input fields that can be changed by the user, we used `MutableStateFlow` from
kotlinx-coroutines (but you can also use `MutableLiveData` from `moko-mvvm-livedata`).
For properties that the UI should keep track of but should not change - use `StateFlow`.
And to notify about the need to do something (show a success message or to go
to another screen) we have created a `Channel` which is exposed on the UI as a `Flow`. All available actions
we combine under a single `sealed interface Action` so that it is known exactly what actions can
tell the given `ViewModel`.

[git commit](https://github.com/Alex009/moko-mvvm-compose-swiftui/commit/d628fb60fedeeb0d259508aa09d3a98ebbc9651c)

## Connect the common ViewModel to Android

On Android, to get our `ViewModel` from `ViewModelStorage` (so that when the screen rotates we
received the same ViewModel) we need to include a special dependency in
`androidApp/build.gradle.kts`:

```kotlin
dependencies {
    // ...
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.4.1")
}
```

Next, add `LoginViewModel` to our screen arguments:
```kotlin
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel()
)
```

Let's replace the local state of the screen with getting the state from the `LoginViewModel`:
```kotlin
val login: String by viewModel.login.collectAsState()
val password: String by viewModel.password.collectAsState()
val isLoading: Boolean by viewModel.isLoading.collectAsState()
val isLoginButtonEnabled: Boolean by viewModel.isButtonEnabled.collectAsState()
```

Subscribe to receive actions from the ViewModel using `observeAsAction` from moko-mvvm:
```kotlin
viewModel.actions.observeAsActions { action ->
    when (action) {
        LoginViewModel.Action.LoginSuccess ->
            Toast.makeText(context, "login success!", Toast.LENGTH_SHORT).show()
    }
}
```

Let's change the input handler of `TextField`s from local state to writing to `ViewModel`:
```kotlin
TextField(
    // ...
    onValueChange = { viewModel.login.value = it }
)
```

And call the button click handler:
```kotlin
Button(
    // ...
    onClick = viewModel::onLoginPressed
) {
    // ...
}
```

We run the application and see that everything works exactly the same as it worked before the common code, but now
all screen logic is controlled by a common ViewModel.

[git commit](https://github.com/Alex009/moko-mvvm-compose-swiftui/commit/a93b9a3b6f1e413bebbba3a30bc5a198ebbf4e84)

## Connect the shared ViewModel to iOS

To connect `LoginViewModel` to SwiftUI, we need Swift add-ons from MOKO MVVM.
They connect via CocoaPods:

```ruby
pod 'mokoMvvmFlowSwiftUI', :podspec => 'https://raw.githubusercontent.com/icerockdev/moko-mvvm/release/0.13.0/mokoMvvmFlowSwiftUI.podspec'
```

And also, in the `LoginViewModel` itself, you need to make changes - from the side of Swift `MutableStateFlow`,
`StateFlow`, `Flow` will lose their generic type since they are interfaces. So that the generic is not lost
you need to use classes. MOKO MVVM provides special `CMutableStateFlow`,
`CStateFlow` and `CFlow` classes to store the generic type in iOS. We bring the types with the following change:

```kotlin
class LoginViewModel : ViewModel() {
    val login: CMutableStateFlow<String> = MutableStateFlow("").cMutableStateFlow()
    val password: CMutableStateFlow<String> = MutableStateFlow("").cMutableStateFlow()

    // ...
    val isLoading: CStateFlow<Boolean> = _isLoading.cStateFlow()

    val isButtonEnabled: CStateFlow<Boolean> =
        // ...
        .cStateFlow()
    
    // ...
    val actions: CFlow<Action> get() = _actions.receiveAsFlow().cFlow()

    // ...
}
```

Now we can move on to the Swift code. To integrate, we make the following change:

```swift
import MultiPlatformLibrary
import mokoMvvmFlowSwiftUI
import Combine

struct LoginScreen: View {
    @ObservedObject var viewModel: LoginViewModel = LoginViewModel()
    @State private var isSuccessfulAlertShowed: Bool = false
    
    // ...
}
```

We add `viewModel` to `View` as `@ObservedObject`, just like we do with Swift versions
ViewModel, but in this case, due to the use of `mokoMvvmFlowSwiftUI` we can immediately pass
Kotlin class `LoginViewModel`.

Next, change the binding of the fields:
```swift
TextField("Login", text: viewModel.binding(\.login))
    .textFieldStyle(.roundedBorder)
    .disabled(viewModel.state(\.isLoading))
```

`mokoMvvmFlowSwiftUI` provides special extension functions to `ViewModel`:
- `binding` returns a `Binding` structure, for the possibility of changing data from the UI side
- `state` returns a value that will be automatically updated when `StateFlow` returns
  new data

Similarly, we replace other places where the local state is used and subscribe to
actions:
```swift
.onReceive(createPublisher(viewModel.actions)) { action in
    let actionKs = LoginViewModelActionKs(action)
    switch(actionKs) {
    case .loginSuccess:
        isSuccessfulAlertShowed = true
        break
    }
}
```

The `createPublisher` function is also provided from `mokoMvvmFlowSwiftUI` and allows you to transform
`CFlow` in `AnyPublisher` from Combine. For reliable processing actions we use
[moko-kswift](https://github.com/icerockdev/moko-kswift). This is a gradle plugin that automatically
generates swift code based on Kotlin. In this case, Swift was generated
`enum LoginViewModelActionKs` from `sealed interface LoginViewModel.Action`. Using automatically
generated `enum` we get a guarantee that the cases in `enum` and `sealed interface` match, so
now we can rely on exhaustive switch logic.
You can read more about MOKO KSwift [in the article](https://medium.com/icerock/how-to-implement-swift-friendly-api-with-kotlin-multiplatform-mobile-e68521a63b6d).

As a result, we got a SwiftUI screen that is controlled from a common code using the MVVM approach.

[git commit](https://github.com/Alex009/moko-mvvm-compose-swiftui/commit/5e260fbf9e4957c6fa5d1679a4282691d37da96a)

## Conclusion

In development with Kotlin Multiplatform Mobile, we consider it important to strive to provide a convenient
toolkit for both platforms - both Android and iOS developers should comfortably develop
and the use of any approach in the common code should not force the developers of one of the platforms
do extra work. By developing our [MOKO](https://moko.icerock.dev) libraries and tools, we strive to simplify the work of developers for both Android and iOS. SwiftUI and MOKO MVVM integration
required a lot of experimentation, but the final result looks comfortable to use.

You can try the project created in this article yourself,
[on GitHub](https://github.com/Alex009/moko-mvvm-compose-swiftui).

We can also [help](https://icerockdev.com/directions/pages/kmm/) and development teams,
who need development assistance or advice on the topic of Kotlin Multiplatform Mobile.
