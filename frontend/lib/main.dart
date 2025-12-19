import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_stripe/flutter_stripe.dart';
import 'package:firebase_core/firebase_core.dart';
import 'core/router/app_router.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Initialize Firebase
  await Firebase.initializeApp();
  
  // Initialize Stripe
  Stripe.publishableKey = "pk_test_YOUR_KEY";
  try {
    await Stripe.instance.applySettings();
  } catch (e) {
    debugPrint("Stripe initialization failed (likely due to placeholder key): $e");
  }

  runApp(
    const ProviderScope(
      child: GetAMotelApp(),
    ),
  );
}

class GetAMotelApp extends StatelessWidget {
  const GetAMotelApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'Get-a-Motel',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(
          seedColor: Colors.indigo,
          primary: Colors.indigo,
        ),
        useMaterial3: true,
        inputDecorationTheme: const InputDecorationTheme(
          filled: true,
          fillColor: Colors.white,
          border: OutlineInputBorder(),
        ),
      ),
      routerConfig: appRouter,
    );
  }
}

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Get-a-Motel'),
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.hotel, size: 64, color: Colors.indigo),
            const SizedBox(height: 16),
            Text(
              'Welcome to Get-a-Motel',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            const SizedBox(height: 8),
            const Text('Multi-tenant booking platform bootstrapped.'),
          ],
        ),
      ),
    );
  }
}
