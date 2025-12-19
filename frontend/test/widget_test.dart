import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:get_a_motel/main.dart';

void main() {
  testWidgets('Home screen shows welcome message', (WidgetTester tester) async {
    await tester.pumpWidget(
      const ProviderScope(
        child: GetAMotelApp(),
      ),
    );

    expect(find.text('Welcome to Get-a-Motel'), findsOneWidget);
    expect(find.text('Multi-tenant booking platform bootstrapped.'), findsOneWidget);
  });
}
