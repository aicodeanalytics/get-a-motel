import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:get_a_motel/features/search/presentation/search_screen.dart';

void main() {
  testWidgets('SearchScreen shows initial filters', (WidgetTester tester) async {
    await tester.pumpWidget(
      const ProviderScope(
        child: MaterialApp(
          home: SearchScreen(),
        ),
      ),
    );

    expect(find.text('City'), findsOneWidget);
    expect(find.text('State'), findsOneWidget);
    expect(find.text('Search Availability'), findsOneWidget);
    expect(find.text('Enter location and dates to find a room.'), findsOneWidget);
  });
}
