//
//  LoginScreen.swift
//  iosApp
//
//  Created by Aleksey Mikhailov on 30.04.2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import MultiPlatformLibrary
import mokoMvvmFlowSwiftUI
import Combine

struct LoginScreen: View {
    @ObservedObject var viewModel: LoginViewModel = LoginViewModel()
    
    @State private var isSuccessfulAlertShowed: Bool = false
    
    var body: some View {
        Group {
            VStack(spacing: 8.0) {
                TextField("Login", text: viewModel.binding(\.login))
                    .textFieldStyle(.roundedBorder)
                    .disabled(viewModel.state(\.isLoading))
                
                SecureField("Password", text: viewModel.binding(\.password))
                    .textFieldStyle(.roundedBorder)
                    .disabled(viewModel.state(\.isLoading))
                
                Button(
                    action: {
                        viewModel.onLoginPressed()
                    }, label: {
                        if viewModel.state(\.isLoading) {
                            ProgressView()
                        } else {
                            Text("Login")
                        }
                    }
                ).disabled(!viewModel.state(\.isButtonEnabled))
            }.padding()
        }.onReceive(createPublisher(viewModel.actions)) { action in
            let actionKs = LoginViewModelActionKs(action)
            switch(actionKs) {
            case .loginSuccess:
                isSuccessfulAlertShowed = true
                break
            }
        }.alert(
            "Login successful",
            isPresented: $isSuccessfulAlertShowed
        ) {
            Button("Close", action: { isSuccessfulAlertShowed = false })
        }
    }
}

struct LoginScreen_Previews: PreviewProvider {
    static var previews: some View {
        LoginScreen()
    }
}
