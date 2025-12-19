import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_stripe/flutter_stripe.dart';
import 'package:uuid/uuid.dart';
import '../repositories/booking_repository.dart';
import '../models/booking_models.dart';
import 'package:go_router/go_router.dart';

class BookingDetailsScreen extends ConsumerStatefulWidget {
  final String roomTypeId;
  const BookingDetailsScreen({super.key, required this.roomTypeId});

  @override
  ConsumerState<BookingDetailsScreen> createState() => _BookingDetailsScreenState();
}

class _BookingDetailsScreenState extends ConsumerState<BookingDetailsScreen> {
  bool _isProcessing = false;

  Future<void> _handleCheckout() async {
    setState(() => _isProcessing = true);
    
    try {
      // 1. Create Booking on Backend
      final request = CreateBookingRequest(
        propertyId: '00000000-0000-0000-0000-000000000000', // Mock property ID
        roomTypeId: widget.roomTypeId,
        checkInDate: DateTime.now().add(const Duration(days: 1)),
        checkOutDate: DateTime.now().add(const Duration(days: 3)),
        numGuests: 1,
        guestEmail: 'guest@example.com',
        guestFirstName: 'John',
        guestLastName: 'Doe',
        idempotencyKey: const Uuid().v4(),
      );

      final response = await ref.read(bookingRepositoryProvider).createBooking(request);

      // 2. Initialize Stripe Payment Sheet
      await Stripe.instance.initPaymentSheet(
        paymentSheetParameters: SetupPaymentSheetParameters(
          paymentIntentClientSecret: response.clientSecret,
          merchantDisplayName: 'Get-a-Motel',
          style: ThemeMode.light,
        ),
      );

      // 3. Present Payment Sheet
      await Stripe.instance.presentPaymentSheet();

      // 4. Success!
      if (mounted) {
        showDialog(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('Booking Confirmed!'),
            content: Text('Your confirmation number is: ${response.confirmationNumber}'),
            actions: [
              TextButton(
                onPressed: () => context.go('/search'),
                child: const Text('OK'),
              ),
            ],
          ),
        );
      }

    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error: $e')),
        );
      }
    } finally {
      setState(() => _isProcessing = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Finalize Booking')),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text('Reservation Summary', style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold)),
            const Divider(height: 32),
            const Card(
              child: Padding(
                padding: EdgeInsets.all(16.0),
                child: Column(
                  children: [
                    ListTile(
                      leading: Icon(Icons.hotel, color: Colors.indigo),
                      title: Text('Motel 6 - Austin North'),
                      subtitle: Text('Austin, TX'),
                    ),
                    ListTile(
                      leading: Icon(Icons.calendar_today, color: Colors.indigo),
                      title: Text('Dec 20 - Dec 22, 2024'),
                      subtitle: Text('2 Nights'),
                    ),
                    ListTile(
                      leading: Icon(Icons.person, color: Colors.indigo),
                      title: Text('1 Guest'),
                    ),
                  ],
                ),
              ),
            ),
            const Spacer(),
            const Padding(
              padding: EdgeInsets.symmetric(vertical: 16.0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text('Total Amount', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                  Text('\$178.00', style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Colors.indigo)),
                ],
              ),
            ),
            ElevatedButton(
              onPressed: _isProcessing ? null : _handleCheckout,
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.all(16),
                backgroundColor: Colors.indigo,
                foregroundColor: Colors.white,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
              ),
              child: _isProcessing 
                ? const CircularProgressIndicator(color: Colors.white) 
                : const Text('Confirm & Pay Securely', style: TextStyle(fontSize: 18)),
            ),
            const SizedBox(height: 12),
            const Text(
              'Your card will be authorized now and captured at check-in.',
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 12, color: Colors.grey),
            ),
          ],
        ),
      ),
    );
  }
}
